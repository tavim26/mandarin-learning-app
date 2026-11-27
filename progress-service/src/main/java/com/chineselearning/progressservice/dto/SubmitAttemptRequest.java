package com.chineselearning.progressservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record SubmitAttemptRequest(
        @NotNull(message = "exerciseId is mandatory")
        @Positive(message = "exerciseId must be positive")
        Long exerciseId,

        @NotNull(message = "submittedAnswer is mandatory")
        @Size(max = 50, message = "submittedAnswer has too many entries")
        Map<String, Object> submittedAnswer
) {
}