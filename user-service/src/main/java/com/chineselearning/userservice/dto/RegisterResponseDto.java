package com.chineselearning.userservice.dto;

import com.chineselearning.userservice.entity.Role;

public record RegisterResponseDto(Long userId, Role role, String fullName) {}