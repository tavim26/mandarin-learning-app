package com.chineselearning.userservice.controller;

import com.chineselearning.userservice.dto.*;
import com.chineselearning.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management")
public class UserController
{
    private final UserService userService;

    public UserController(UserService userService)
    {
        this.userService = userService;
    }


    // ===== Users =====

    @PostMapping
    @Operation(summary = "Create a STUDENT or TEACHER account (admin only)")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequestDto request)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @GetMapping
    @Operation(summary = "List all users (admin only)")
    public List<UserDto> getAllUsers()
    {
        return userService.getAllUsers();
    }

    @GetMapping("/me")
    @Operation(summary = "Get the authenticated user")
    public UserDto getCurrentUser(@Parameter(hidden = true) @AuthenticationPrincipal Long userId)
    {
        return userService.getUserById(userId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user (admin only)")
    public UserDto getUserById(@PathVariable Long id)
    {
        return userService.getUserById(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Search users by name (admin only)")
    public List<UserDto> searchUsers(
            @RequestParam @NotBlank(message = "Search text is required")
            @Size(max = 100, message = "Search text must not exceed 100 characters") String name)
    {
        return userService.searchUsersByName(name);
    }

    @PutMapping("/{id}/name")
    @Operation(summary = "Rename a user (admin only)")
    public UserDto updateUserName(
            @PathVariable Long id,
            @RequestParam @NotBlank(message = "Full name is required")
            @Size(max = 100, message = "Full name must not exceed 100 characters") String newName)
    {
        return userService.updateUserName(id, newName);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user and their profile (admin only)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id)
    {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }



    // ===== Students =====

    @GetMapping("/students")
    @Operation(summary = "List all students with profile data (admin only)")
    public List<StudentProfileDto> getAllStudents()
    {
        return userService.getAllStudents();
    }

    @GetMapping("/students/search")
    @Operation(summary = "Search students by nickname")
    public List<StudentDto> searchStudentsByNickname(
            @RequestParam @NotBlank(message = "Search text is required")
            @Size(max = 50, message = "Search text must not exceed 50 characters") String nickname)
    {
        return userService.searchStudentsByNickname(nickname);
    }

    @GetMapping("/students/{userId}")
    @Operation(summary = "Get a student's profile (self or admin)")
    public StudentDto getStudent(@PathVariable Long userId)
    {
        return userService.getStudentById(userId);
    }

    @PutMapping("/students/{userId}/nickname")
    @Operation(summary = "Set a student's nickname (self or admin)")
    public StudentDto updateStudentNickname(
            @PathVariable Long userId,
            @RequestParam @NotBlank(message = "Nickname is required")
            @Size(min = 2, max = 50, message = "Nickname must be between 2 and 50 characters") String newNickname)
    {
        return userService.updateStudentNickname(userId, newNickname);
    }



    // ===== Teachers =====

    @GetMapping("/teachers")
    @Operation(summary = "List all teachers with profile data (admin only)")
    public List<TeacherProfileDto> getAllTeachers()
    {
        return userService.getAllTeachers();
    }

    @GetMapping("/teachers/{userId}")
    @Operation(summary = "Get a teacher's profile (self or admin)")
    public TeacherDto getTeacher(@PathVariable Long userId)
    {
        return userService.getTeacherById(userId);
    }

    @PutMapping("/teachers/{userId}/title")
    @Operation(summary = "Set a teacher's title (self or admin)")
    public TeacherDto updateTeacherTitle(
            @PathVariable Long userId,
            @RequestParam @NotBlank(message = "Title is required")
            @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters") String newTitle)
    {
        return userService.updateTeacherTitle(userId, newTitle);
    }



    // ===== Credentials =====

    @PutMapping("/{id}/email")
    @Operation(summary = "Change the email (self or admin)")
    public UserDto updateEmail(
            @PathVariable Long id,
            @RequestParam @NotBlank(message = "Email is required") @Email(message = "Invalid email format")
            @Size(max = 255, message = "Email must not exceed 255 characters") String newEmail)
    {
        return userService.updateEmail(id, newEmail);
    }

    @PutMapping("/{id}/password")
    @Operation(summary = "Change the password (self or admin)")
    public ResponseEntity<Void> updatePassword(@PathVariable Long id,
                                               @Valid @RequestBody ChangePasswordRequest request)
    {
        userService.updatePassword(id, request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/password/reset")
    @Operation(summary = "Reset a user's password (admin only)")
    public ResponseEntity<Void> adminResetPassword(@PathVariable Long id,
                                                   @Valid @RequestBody ResetPasswordRequest request)
    {
        userService.adminUpdatePassword(id, request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/ban")
    @Operation(summary = "Ban a user (admin only)")
    public ResponseEntity<Void> banUser(@PathVariable Long id)
    {
        userService.banUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/unban")
    @Operation(summary = "Lift a ban (admin only)")
    public ResponseEntity<Void> unbanUser(@PathVariable Long id)
    {
        userService.unbanUser(id);
        return ResponseEntity.noContent().build();
    }
}