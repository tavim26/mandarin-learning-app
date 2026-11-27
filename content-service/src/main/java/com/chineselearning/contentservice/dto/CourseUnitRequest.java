package com.chineselearning.contentservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CourseUnitRequest(
        @NotBlank(message = "Unit title is mandatory")
        @Size(max = 255, message = "Unit title must not exceed 255 characters")
        String title,

        @Size(max = 5000, message = "Description must not exceed 5000 characters")
        String description,

        @Min(value = 1, message = "HSK level must be between 1 and 6")
        @Max(value = 6, message = "HSK level must be between 1 and 6")
        Integer hskLevel,

        @NotNull(message = "Order index is mandatory")
        @Min(value = 0, message = "Order index cannot be negative")
        Integer orderIndex
) {}