package com.chineselearning.flashcardservice.dto;

import com.chineselearning.flashcardservice.entity.Flashcard;

public record FlashcardDto(Long id, Long setId, String frontText, String backText) {

    public static FlashcardDto from(Flashcard card) {
        return new FlashcardDto(card.getId(), card.getSet().getId(), card.getFrontText(), card.getBackText());
    }
}