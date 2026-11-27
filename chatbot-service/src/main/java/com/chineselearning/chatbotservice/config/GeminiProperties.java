package com.chineselearning.chatbotservice.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "gemini")
public record GeminiProperties(
        @NotBlank String model,
        Api api,
        Duration connectTimeout,
        Duration readTimeout,
        @Min(1) int maxAttempts,
        @Min(256) int maxOutputTokens
) {
    public record Api(@NotBlank String key, @NotBlank String baseUrl) {
    }
}