package com.chineselearning.flashcardservice.dto;

import com.chineselearning.flashcardservice.entity.FlashcardReview;

import java.time.LocalDateTime;

public record FlashcardReviewDto(Long id, Long studentId, Long flashcardId, LocalDateTime reviewedAt, Integer quality) {

    public static FlashcardReviewDto from(FlashcardReview r) {
        return new FlashcardReviewDto(r.getId(), r.getStudentId(), r.getFlashcard().getId(),
                r.getReviewedAt(), r.getQuality());
    }
}