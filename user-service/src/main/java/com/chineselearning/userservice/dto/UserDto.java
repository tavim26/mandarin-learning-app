package com.chineselearning.userservice.dto;

import com.chineselearning.userservice.entity.Role;
import com.chineselearning.userservice.entity.UserEntity;

public record UserDto(Long id, String fullName, Role role, boolean banned)
{
    public static UserDto from(UserEntity user)
    {
        return new UserDto(
                user.getId(),
                user.getFullName(),
                user.getCredential().getRole(),
                !user.getCredential().isActive()
        );
    }
}