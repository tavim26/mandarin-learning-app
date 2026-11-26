package com.chineselearning.userservice.dto;

import com.chineselearning.userservice.entity.Role;

public record AuthResponseDto(String token, Long userId, Role role, String fullName) {}