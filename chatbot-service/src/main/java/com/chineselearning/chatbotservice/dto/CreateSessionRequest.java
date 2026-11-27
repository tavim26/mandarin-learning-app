package com.chineselearning.chatbotservice.dto;

import jakarta.validation.constraints.Size;

public record CreateSessionRequest(
        @Size(max = 255) String title,
        @Size(max = 1000) String customInstructions
) {
}