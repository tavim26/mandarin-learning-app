package com.chineselearning.userservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class RegisterResponseDto
{
    private Long userId;
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "STUDENT|TEACHER", message = "Role must be STUDENT or TEACHER")
    private String role;
    private String fullName;

    public RegisterResponseDto(Long userId, String role, String fullName)
    {
        this.userId = userId;
        this.role = role;
        this.fullName = fullName;
    }

    public Long getUserId() { return userId; }
    public String getRole() { return role; }
    public String getFullName() { return fullName; }
}