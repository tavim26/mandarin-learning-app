package com.chineselearning.progressservice.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "content-service")
public record ContentServiceProperties(
        @NotBlank String url,
        Duration connectTimeout,
        Duration readTimeout
) {
}