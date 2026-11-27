package com.chineselearning.flashcardservice.service;

import com.chineselearning.flashcardservice.dto.CreateFlashcardRequest;
import com.chineselearning.flashcardservice.dto.CreateFlashcardSetRequest;
import com.chineselearning.flashcardservice.dto.FlashcardDto;
import com.chineselearning.flashcardservice.dto.FlashcardSetDto;
import com.chineselearning.flashcardservice.dto.UpdateFlashcardRequest;
import com.chineselearning.flashcardservice.dto.UpdateFlashcardSetRequest;
import com.chineselearning.flashcardservice.entity.Flashcard;
import com.chineselearning.flashcardservice.entity.FlashcardSet;
import com.chineselearning.flashcardservice.exception.QuotaExceededException;
import com.chineselearning.flashcardservice.repository.FlashcardProgressRepository;
import com.chineselearning.flashcardservice.repository.FlashcardRepository;
import com.chineselearning.flashcardservice.repository.FlashcardReviewRepository;
import com.chineselearning.flashcardservice.repository.FlashcardSetRepository;
import com.chineselearning.flashcardservice.repository.SetCardCount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FlashcardSetService {

    static final int MAX_SETS_PER_STUDENT = 200;
    static final int MAX_CARDS_PER_SET = 2_000;

    private final FlashcardSetRepository setRepository;
    private final FlashcardRepository flashcardRepository;
    private final FlashcardProgressRepository progressRepository;
    private final FlashcardReviewRepository reviewRepository;
    private final FlashcardAccess access;

    public FlashcardSetService(FlashcardSetRepository setRepository,
                               FlashcardRepository flashcardRepository,
                               FlashcardProgressRepository progressRepository,
                               FlashcardReviewRepository reviewRepository,
                               FlashcardAccess access) {
        this.setRepository = setRepository;
        this.flashcardRepository = flashcardRepository;
        this.progressRepository = progressRepository;
        this.reviewRepository = reviewRepository;
        this.access = access;
    }

    // ---------- sets ----------

    @Transactional
    public FlashcardSetDto createSet(Long userId, CreateFlashcardSetRequest request) {
        if (setRepository.countByStudentId(userId) >= MAX_SETS_PER_STUDENT) {
            throw new QuotaExceededException("You can have at most " + MAX_SETS_PER_STUDENT + " flashcard sets");
        }
        FlashcardSet saved = setRepository.save(
                FlashcardSet.create(userId, request.title(), request.description()));
        return FlashcardSetDto.from(saved, 0);
    }

    @Transactional(readOnly = true)
    public List<FlashcardSetDto> getSetsByStudent(Long userId, Long studentId) {
        access.requireSelf(userId, studentId);
        List<FlashcardSet> sets = setRepository.findByStudentIdOrderByIdDesc(studentId);
        if (sets.isEmpty()) {
            return List.of();
        }
        Map<Long, Long> counts = flashcardRepository
                .countBySetIds(sets.stream().map(FlashcardSet::getId).toList()).stream()
                .collect(Collectors.toMap(SetCardCount::getSetId, SetCardCount::getTotal));
        return sets.stream()
                .map(set -> FlashcardSetDto.from(set, counts.getOrDefault(set.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public FlashcardSetDto getSet(Long userId, Long setId) {
        FlashcardSet set = access.requireOwnedSet(userId, setId);
        return FlashcardSetDto.from(set, flashcardRepository.countBySetId(setId));
    }

    @Transactional
    public FlashcardSetDto updateSet(Long userId, Long setId, UpdateFlashcardSetRequest request) {
        FlashcardSet set = access.requireOwnedSet(userId, setId);
        set.update(request.title(), request.description()); // dirty checking persists it
        return FlashcardSetDto.from(set, flashcardRepository.countBySetId(setId));
    }

    @Transactional
    public void deleteSet(Long userId, Long setId) {
        access.requireOwnedSet(userId, setId);
        // Bulk deletes in dependency order: no entity is loaded into memory.
        reviewRepository.deleteAllBySetId(setId);
        progressRepository.deleteAllBySetId(setId);
        flashcardRepository.deleteAllBySetId(setId);
        setRepository.deleteById(setId);
    }

    // ---------- cards ----------

    @Transactional
    public FlashcardDto createCard(Long userId, CreateFlashcardRequest request) {
        FlashcardSet set = access.requireOwnedSet(userId, request.setId());
        if (flashcardRepository.countBySetId(set.getId()) >= MAX_CARDS_PER_SET) {
            throw new QuotaExceededException("A set can have at most " + MAX_CARDS_PER_SET + " cards");
        }
        Flashcard saved = flashcardRepository.save(
                Flashcard.create(set, request.frontText(), request.backText()));
        return FlashcardDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<FlashcardDto> getCardsBySet(Long userId, Long setId) {
        access.requireOwnedSet(userId, setId);
        return flashcardRepository.findBySetIdOrderByIdAsc(setId).stream().map(FlashcardDto::from).toList();
    }

    @Transactional(readOnly = true)
    public FlashcardDto getCard(Long userId, Long flashcardId) {
        return FlashcardDto.from(access.requireOwnedCard(userId, flashcardId));
    }

    @Transactional
    public FlashcardDto updateCard(Long userId, Long flashcardId, UpdateFlashcardRequest request) {
        Flashcard card = access.requireOwnedCard(userId, flashcardId);
        card.update(request.frontText(), request.backText());
        return FlashcardDto.from(card);
    }

    @Transactional
    public void deleteCard(Long userId, Long flashcardId) {
        access.requireOwnedCard(userId, flashcardId);
        reviewRepository.deleteAllByFlashcardId(flashcardId);
        progressRepository.deleteAllByFlashcardId(flashcardId);
        flashcardRepository.deleteById(flashcardId);
    }
}