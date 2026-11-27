package com.chineselearning.flashcardservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SubmitReviewRequest(
        @NotNull(message = "Flashcard id is mandatory")
        @Positive(message = "Flashcard id must be positive")
        Long flashcardId,

        @NotNull(message = "Quality is mandatory")
        @Min(value = 0, message = "Quality must be between 0 and 5")
        @Max(value = 5, message = "Quality must be between 0 and 5")
        Integer quality
) {
}