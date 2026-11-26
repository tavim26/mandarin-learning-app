package com.chineselearning.contentservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MaterialRequest(
        @NotNull(message = "Lesson id is mandatory")
        Long lessonId,

        @NotBlank(message = "Material title is mandatory")
        @Size(max = 255, message = "Material title must not exceed 255 characters")
        String title,

        @NotBlank(message = "Material type is mandatory")
        @Size(max = 20, message = "Material type is too long")
        String type,

        @Size(max = 1000, message = "Material URL must not exceed 1000 characters")
        String url
) {}