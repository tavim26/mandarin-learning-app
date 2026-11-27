package com.chineselearning.contentservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExerciseRequest(

        Long lessonId,

        @NotBlank(message = "Exercise type is mandatory")
        @Pattern(regexp = "MULTIPLE_CHOICE|TRANSLATION|FILL_BLANK|MATCHING|ORDERING",
                message = "Unknown exercise type")
        String type,

        @NotBlank(message = "Exercise prompt is mandatory")
        @Size(max = 2000, message = "Prompt must not exceed 2000 characters")
        String prompt,

        @Min(value = 1, message = "Difficulty must be between 1 and 5")
        @Max(value = 5, message = "Difficulty must be between 1 and 5")
        Integer difficulty,

        // The shape depends on the type and is checked by ExerciseContentValidator.
        @NotNull(message = "Exercise content is mandatory")
        Map<String, Object> contentData
) {}