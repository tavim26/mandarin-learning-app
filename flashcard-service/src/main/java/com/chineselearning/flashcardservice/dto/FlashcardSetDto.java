package com.chineselearning.flashcardservice.dto;

import com.chineselearning.flashcardservice.entity.FlashcardSet;

public record FlashcardSetDto(Long id, Long studentId, String title, String description, Integer cardCount) {

    public static FlashcardSetDto from(FlashcardSet set, long cardCount) {
        return new FlashcardSetDto(set.getId(), set.getStudentId(), set.getTitle(), set.getDescription(),
                (int) cardCount);
    }
}