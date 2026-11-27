package com.chineselearning.flashcardservice.dto;

import com.chineselearning.flashcardservice.entity.FlashcardProgress;
import com.chineselearning.flashcardservice.scheduling.Sm2Scheduler;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FlashcardProgressDto(
        Long id,
        Long studentId,
        Long flashcardId,
        BigDecimal easinessFactor,
        Integer intervalDays,
        Integer repetitionCount,
        LocalDateTime nextReviewAt,
        LocalDateTime lastReviewedAt
) {
    public static FlashcardProgressDto from(FlashcardProgress p) {
        return new FlashcardProgressDto(p.getId(), p.getStudentId(), p.getFlashcard().getId(),
                p.getEasinessFactor(), p.getIntervalDays(), p.getRepetitionCount(),
                p.getNextReviewAt(), p.getLastReviewedAt());
    }

    public static FlashcardProgressDto unseen(Long studentId, Long flashcardId) {
        return new FlashcardProgressDto(null, studentId, flashcardId,
                Sm2Scheduler.INITIAL_EASINESS_FACTOR, 0, 0, null, null);
    }
}