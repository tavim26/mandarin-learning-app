package com.chineselearning.flashcardservice.service;

import com.chineselearning.flashcardservice.entity.Flashcard;
import com.chineselearning.flashcardservice.entity.FlashcardSet;
import com.chineselearning.flashcardservice.exception.ForbiddenOperationException;
import com.chineselearning.flashcardservice.exception.ResourceNotFoundException;
import com.chineselearning.flashcardservice.repository.FlashcardRepository;
import com.chineselearning.flashcardservice.repository.FlashcardSetRepository;
import org.springframework.stereotype.Component;

@Component
public class FlashcardAccess {

    private final FlashcardSetRepository setRepository;
    private final FlashcardRepository flashcardRepository;

    public FlashcardAccess(FlashcardSetRepository setRepository, FlashcardRepository flashcardRepository)
    {
        this.setRepository = setRepository;
        this.flashcardRepository = flashcardRepository;
    }

    public void requireSelf(Long userId, Long studentId)
    {
        if (!userId.equals(studentId))
        {
            throw new ForbiddenOperationException("You cannot access another student's flashcards");
        }
    }

    public FlashcardSet requireOwnedSet(Long userId, Long setId)
    {
        FlashcardSet set = setRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard set " + setId + " does not exist"));

        if (!set.isOwnedBy(userId))
        {
            throw new ForbiddenOperationException("You do not have access to flashcard set " + setId);
        }
        return set;
    }

    public Flashcard requireOwnedCard(Long userId, Long flashcardId)
    {
        Flashcard card = flashcardRepository.findWithSetById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard " + flashcardId + " does not exist"));

        if (!card.getSet().isOwnedBy(userId))
        {
            throw new ForbiddenOperationException("You do not have access to flashcard " + flashcardId);
        }
        return card;
    }
}