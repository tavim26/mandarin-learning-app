package com.chineselearning.contentservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LessonRequest(
        @NotNull(message = "Unit id is mandatory")
        Long unitId,

        @NotBlank(message = "Lesson title is mandatory")
        @Size(max = 255, message = "Lesson title must not exceed 255 characters")
        String title,

        @Size(max = 5000, message = "Description must not exceed 5000 characters")
        String description,

        @NotNull(message = "XP reward is mandatory")
        @Min(value = 0, message = "XP reward cannot be negative")
        @Max(value = 1000, message = "XP reward must not exceed 1000")
        Integer xpReward,

        @NotNull(message = "Order index is mandatory")
        @Min(value = 0, message = "Order index cannot be negative")
        Integer orderIndex
) {}