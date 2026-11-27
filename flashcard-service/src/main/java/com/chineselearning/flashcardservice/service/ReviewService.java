package com.chineselearning.flashcardservice.service;

import com.chineselearning.flashcardservice.dto.FlashcardProgressDto;
import com.chineselearning.flashcardservice.dto.FlashcardReviewDto;
import com.chineselearning.flashcardservice.dto.ReviewResultDto;
import com.chineselearning.flashcardservice.dto.SubmitReviewRequest;
import com.chineselearning.flashcardservice.entity.Flashcard;
import com.chineselearning.flashcardservice.entity.FlashcardProgress;
import com.chineselearning.flashcardservice.entity.FlashcardReview;
import com.chineselearning.flashcardservice.exception.ResourceNotFoundException;
import com.chineselearning.flashcardservice.repository.FlashcardProgressRepository;
import com.chineselearning.flashcardservice.repository.FlashcardRepository;
import com.chineselearning.flashcardservice.repository.FlashcardReviewRepository;
import com.chineselearning.flashcardservice.scheduling.Sm2Scheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final FlashcardRepository flashcardRepository;
    private final FlashcardProgressRepository progressRepository;
    private final FlashcardReviewRepository reviewRepository;
    private final FlashcardAccess access;
    private final Clock clock;

    public ReviewService(FlashcardRepository flashcardRepository,
                         FlashcardProgressRepository progressRepository,
                         FlashcardReviewRepository reviewRepository,
                         FlashcardAccess access,
                         Clock clock) {
        this.flashcardRepository = flashcardRepository;
        this.progressRepository = progressRepository;
        this.reviewRepository = reviewRepository;
        this.access = access;
        this.clock = clock;
    }

    @Transactional
    public ReviewResultDto submitReview(Long userId, SubmitReviewRequest request) {
        Flashcard card = access.requireOwnedCard(userId, request.flashcardId());

        // Race-free: create the row if it is missing, then lock it so concurrent reviews run one by one.
        progressRepository.insertIfAbsent(userId, card.getId());
        FlashcardProgress progress = progressRepository.findForUpdate(userId, card.getId())
                .orElseThrow(() -> new IllegalStateException("Progress row missing after insert"));

        LocalDateTime now = LocalDateTime.now(clock);
        Sm2Scheduler.Result result = Sm2Scheduler.schedule(
                progress.getEasinessFactor(),
                progress.getIntervalDays(),
                progress.getRepetitionCount(),
                request.quality(),
                now.toLocalDate());
        progress.apply(result, now); // dirty checking persists it

        FlashcardReview review = reviewRepository.save(FlashcardReview.of(userId, card, request.quality(), now));
        return new ReviewResultDto(FlashcardReviewDto.from(review), FlashcardProgressDto.from(progress));
    }

    @Transactional(readOnly = true)
    public List<FlashcardProgressDto> getDueFlashcards(Long userId, Long studentId, Long setId) {
        access.requireSelf(userId, studentId);
        access.requireOwnedSet(userId, setId);

        List<Long> cardIds = flashcardRepository.findIdsBySetId(setId);
        if (cardIds.isEmpty()) {
            return List.of();
        }
        List<FlashcardProgress> existing = progressRepository.findByStudentIdAndFlashcardIdIn(studentId, cardIds);
        Set<Long> seen = existing.stream().map(p -> p.getFlashcard().getId()).collect(Collectors.toSet());
        LocalDateTime now = LocalDateTime.now(clock);

        List<FlashcardProgressDto> result = new ArrayList<>();
        existing.stream()
                .filter(p -> p.getNextReviewAt() != null && !p.getNextReviewAt().isAfter(now))
                .sorted(Comparator.comparing(FlashcardProgress::getNextReviewAt).thenComparing(FlashcardProgress::getId))
                .map(FlashcardProgressDto::from)
                .forEach(result::add);
        cardIds.stream()
                .filter(id -> !seen.contains(id))
                .map(id -> FlashcardProgressDto.unseen(studentId, id))
                .forEach(result::add);
        return result;
    }

    @Transactional(readOnly = true)
    public List<FlashcardReviewDto> getReviewHistory(Long userId, Long studentId, Long flashcardId) {
        access.requireSelf(userId, studentId);
        return reviewRepository.findByStudentIdAndFlashcardIdOrderByReviewedAtAscIdAsc(studentId, flashcardId)
                .stream().map(FlashcardReviewDto::from).toList();
    }

    @Transactional(readOnly = true)
    public FlashcardProgressDto getProgress(Long userId, Long studentId, Long flashcardId) {
        access.requireSelf(userId, studentId);
        return progressRepository.findByStudentIdAndFlashcardId(studentId, flashcardId)
                .map(FlashcardProgressDto::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No progress for student " + studentId + " and flashcard " + flashcardId));
    }
}