package com.chineselearning.chatbotservice.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "chatbot")
public record ChatbotProperties(Context context, RateLimit rateLimit) {

    public record Context(@Min(1) int windowSize) {
    }

    public record RateLimit(@Min(1) int maxMessages, Duration window) {
    }
}