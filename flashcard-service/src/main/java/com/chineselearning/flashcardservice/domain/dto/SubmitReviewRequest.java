package com.chineselearning.flashcardservice.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SubmitReviewRequest {

    @NotNull(message = "Flashcard id is mandatory")
    @Positive(message = "Flashcard id must be positive")
    private Long flashcardId;

    @NotNull(message = "Quality is mandatory")
    @Min(value = 0, message = "Quality must be between 0 and 5")
    @Max(value = 5, message = "Quality must be between 0 and 5")
    private Integer quality;

    public SubmitReviewRequest() {}

    public Long getFlashcardId() { return flashcardId; }
    public void setFlashcardId(Long flashcardId) { this.flashcardId = flashcardId; }

    public Integer getQuality() { return quality; }
    public void setQuality(Integer quality) { this.quality = quality; }
}