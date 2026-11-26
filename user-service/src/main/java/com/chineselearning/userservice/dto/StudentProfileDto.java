package com.chineselearning.userservice.dto;

import com.chineselearning.userservice.entity.Role;
import com.chineselearning.userservice.entity.UserEntity;

public record StudentProfileDto(Long userId, String fullName, Role role, String nickname, String email, boolean banned)
{
    public static StudentProfileDto from(UserEntity user)
    {
        return new StudentProfileDto(
                user.getId(),
                user.getFullName(),
                user.getCredential().getRole(),
                user.getStudent() != null ? user.getStudent().getNickname() : null,
                user.getCredential().getEmail(),
                !user.getCredential().isActive()
        );
    }
}