package com.chineselearning.userservice.dto;

import com.chineselearning.userservice.entity.Role;
import com.chineselearning.userservice.entity.UserEntity;

public record TeacherProfileDto(Long userId, String fullName, Role role, String title, String email, boolean banned)
{
    public static TeacherProfileDto from(UserEntity user)
    {
        return new TeacherProfileDto(
                user.getId(),
                user.getFullName(),
                user.getCredential().getRole(),
                user.getTeacher() != null ? user.getTeacher().getTitle() : null,
                user.getCredential().getEmail(),
                !user.getCredential().isActive()
        );
    }
}