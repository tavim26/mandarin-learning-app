package com.chineselearning.userservice.service;

import com.chineselearning.userservice.dto.*;
import com.chineselearning.userservice.entity.*;
import com.chineselearning.userservice.exception.ConflictException;
import com.chineselearning.userservice.exception.ResourceNotFoundException;
import com.chineselearning.userservice.repository.CredentialRepository;
import com.chineselearning.userservice.repository.StudentRepository;
import com.chineselearning.userservice.repository.TeacherRepository;
import com.chineselearning.userservice.repository.UserRepository;
import com.chineselearning.userservice.util.Emails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService
{
    private final CredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(CredentialRepository credentialRepository, UserRepository userRepository, StudentRepository studentRepository, TeacherRepository teacherRepository, PasswordEncoder passwordEncoder)
    {
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ===== Account creation =====

    @Transactional
    public CredentialEntity createAccount(String email, String rawPassword, String fullName, Role role)
    {
        String normalizedEmail = Emails.normalize(email);
        if (credentialRepository.existsByEmail(normalizedEmail))
        {
            throw new ConflictException("Email already registered");
        }

        CredentialEntity credential = new CredentialEntity(normalizedEmail, passwordEncoder.encode(rawPassword), role);
        UserEntity user = new UserEntity(fullName.trim());
        credential.attachUser(user);

        switch (role)
        {
            case STUDENT -> user.attachStudent(new StudentEntity());
            case TEACHER -> user.attachTeacher(new TeacherEntity());
            case ADMIN -> { }
        }

        return credentialRepository.saveAndFlush(credential);
    }


    @Transactional
    public UserDto createUser(CreateUserRequestDto request)
    {
        Role role = Role.valueOf(request.getRole());
        CredentialEntity credential = createAccount(request.getEmail(), request.getPassword(), request.getFullName(), role);
        return UserDto.from(credential.getUser());
    }


    // ===== Users =====

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers()
    {
        return userRepository.findAllByOrderByIdAsc().stream().map(UserDto::from).toList();
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id)
    {
        return UserDto.from(findUser(id));
    }

    @Transactional(readOnly = true)
    public List<UserDto> searchUsersByName(String fragment)
    {
        return userRepository.findByFullNameContainingIgnoreCaseOrderByIdAsc(fragment.trim()).stream()
                .map(UserDto::from)
                .toList();
    }

    @Transactional
    public UserDto updateUserName(Long id, String newName)
    {
        UserEntity user = findUser(id);
        user.setFullName(newName.trim());
        return UserDto.from(user);
    }

    @Transactional
    public void deleteUser(Long id)
    {
        CredentialEntity credential = findCredential(id);
        requireNotAdmin(credential, "Admin accounts cannot be deleted");
        credentialRepository.delete(credential);
    }


    // ===== Students and teachers =====

    @Transactional(readOnly = true)
    public List<StudentProfileDto> getAllStudents()
    {
        return userRepository.findByCredential_RoleOrderByIdAsc(Role.STUDENT).stream()
                .map(StudentProfileDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TeacherProfileDto> getAllTeachers()
    {
        return userRepository.findByCredential_RoleOrderByIdAsc(Role.TEACHER).stream()
                .map(TeacherProfileDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentDto getStudentById(Long userId)
    {
        return StudentDto.from(findStudent(userId));
    }

    @Transactional(readOnly = true)
    public List<StudentDto> searchStudentsByNickname(String fragment)
    {
        return studentRepository.findByNicknameContainingIgnoreCase(fragment.trim()).stream()
                .map(StudentDto::from)
                .toList();
    }

    @Transactional
    public StudentDto updateStudentNickname(Long userId, String newNickname)
    {
        StudentEntity student = findStudent(userId);
        String nickname = newNickname.trim();

        if (studentRepository.existsByNicknameAndUserIdNot(nickname, userId))
        {
            throw new ConflictException("Nickname already in use");
        }

        student.setNickname(nickname);
        return StudentDto.from(student);
    }

    @Transactional(readOnly = true)
    public TeacherDto getTeacherById(Long userId)
    {
        return TeacherDto.from(findTeacher(userId));
    }

    @Transactional
    public TeacherDto updateTeacherTitle(Long userId, String newTitle)
    {
        TeacherEntity teacher = findTeacher(userId);
        teacher.setTitle(newTitle.trim());
        return TeacherDto.from(teacher);
    }


    // ===== Credentials =====

    @Transactional
    public UserDto updateEmail(Long id, String newEmail)
    {
        CredentialEntity credential = findCredential(id);
        String email = Emails.normalize(newEmail);

        if (!email.equals(credential.getEmail()))
        {
            if (credentialRepository.existsByEmail(email))
            {
                throw new ConflictException("Email already in use");
            }
            credential.setEmail(email);
        }
        return UserDto.from(credential.getUser());
    }

    @Transactional
    public void updatePassword(Long id, String oldPassword, String newPassword)
    {
        CredentialEntity credential = findCredential(id);

        if (!passwordEncoder.matches(oldPassword, credential.getPasswordHash()))
        {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        credential.setPasswordHash(passwordEncoder.encode(newPassword));
        credential.revokeTokens();
    }

    @Transactional
    public void adminUpdatePassword(Long id, String newPassword)
    {
        CredentialEntity credential = findCredential(id);
        credential.setPasswordHash(passwordEncoder.encode(newPassword));
        credential.revokeTokens();
    }

    @Transactional
    public void banUser(Long id)
    {
        CredentialEntity credential = findCredential(id);
        requireNotAdmin(credential, "Admin accounts cannot be banned");

        if (!credential.isActive())
        {
            throw new ConflictException("User is already banned");
        }

        credential.setActive(false);
        credential.revokeTokens();
    }

    @Transactional
    public void unbanUser(Long id)
    {
        CredentialEntity credential = findCredential(id);

        if (credential.isActive())
        {
            throw new ConflictException("User is not banned");
        }

        credential.setActive(true);
    }

    @Transactional(readOnly = true)
    public TokenStatusDto getTokenStatus(Long userId)
    {
        CredentialEntity credential = findCredential(userId);
        return new TokenStatusDto(credential.isActive(), credential.getTokenVersion());
    }


    // ===== Helpers =====

    private UserEntity findUser(Long id)
    {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private CredentialEntity findCredential(Long id)
    {
        return credentialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private StudentEntity findStudent(Long userId)
    {
        return studentRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + userId));
    }

    private TeacherEntity findTeacher(Long userId)
    {
        return teacherRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found: " + userId));
    }

    private static void requireNotAdmin(CredentialEntity credential, String message)
    {
        if (credential.getRole() == Role.ADMIN)
        {
            throw new IllegalArgumentException(message);
        }
    }
}