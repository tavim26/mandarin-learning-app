package com.chineselearning.flashcardservice.dto;

import java.math.BigDecimal;

public record FlashcardSetStatsDto(
        Long setId,
        Integer totalCards,
        Integer newCards,
        Integer learningCards,
        Integer matureCards,
        Integer dueToday,
        BigDecimal averageEasinessFactor
) {
}