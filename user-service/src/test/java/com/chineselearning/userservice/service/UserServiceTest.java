package com.chineselearning.userservice.service;

import com.chineselearning.userservice.entity.CredentialEntity;
import com.chineselearning.userservice.entity.Role;
import com.chineselearning.userservice.entity.StudentEntity;
import com.chineselearning.userservice.exception.ConflictException;
import com.chineselearning.userservice.repository.CredentialRepository;
import com.chineselearning.userservice.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest
{
    @Mock CredentialRepository credentialRepository;
    @Mock StudentRepository studentRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks UserService userService;

    @Test
    void duplicateEmailIsDetectedRegardlessOfCase()
    {
        when(credentialRepository.existsByEmail("ana@test.com")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> userService.createAccount("  Ana@Test.com ", "Parola123", "Ana", Role.STUDENT));
    }

    @Test
    void adminAccountsCannotBeBanned()
    {
        CredentialEntity admin = new CredentialEntity("admin@test.com", "hash", Role.ADMIN);
        when(credentialRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(IllegalArgumentException.class, () -> userService.banUser(1L));
    }

    @Test
    void keepingTheSameNicknameIsAllowed()
    {
        StudentEntity student = new StudentEntity();
        student.setNickname("ana");
        when(studentRepository.findById(5L)).thenReturn(Optional.of(student));
        when(studentRepository.existsByNicknameAndUserIdNot("ana", 5L)).thenReturn(false);

        assertEquals("ana", userService.updateStudentNickname(5L, " ana ").nickname());
    }

    @Test
    void changingThePasswordRevokesExistingTokens()
    {
        CredentialEntity credential = new CredentialEntity("ana@test.com", "old-hash", Role.STUDENT);
        when(credentialRepository.findById(2L)).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("OldPass123", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("NewPass123")).thenReturn("new-hash");

        userService.updatePassword(2L, "OldPass123", "NewPass123");

        assertEquals("new-hash", credential.getPasswordHash());
        assertEquals(1, credential.getTokenVersion());
    }
}