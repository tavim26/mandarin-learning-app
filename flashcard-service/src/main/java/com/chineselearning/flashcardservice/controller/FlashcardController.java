package com.chineselearning.flashcardservice.controller;

import com.chineselearning.flashcardservice.dto.CreateFlashcardRequest;
import com.chineselearning.flashcardservice.dto.CreateFlashcardSetRequest;
import com.chineselearning.flashcardservice.dto.FlashcardDto;
import com.chineselearning.flashcardservice.dto.FlashcardProgressDto;
import com.chineselearning.flashcardservice.dto.FlashcardReviewDto;
import com.chineselearning.flashcardservice.dto.FlashcardSetDto;
import com.chineselearning.flashcardservice.dto.FlashcardSetStatsDto;
import com.chineselearning.flashcardservice.dto.ReviewResultDto;
import com.chineselearning.flashcardservice.dto.SubmitReviewRequest;
import com.chineselearning.flashcardservice.dto.TotalDueStatsDto;
import com.chineselearning.flashcardservice.dto.UpdateFlashcardRequest;
import com.chineselearning.flashcardservice.dto.UpdateFlashcardSetRequest;
import com.chineselearning.flashcardservice.service.FlashcardSetService;
import com.chineselearning.flashcardservice.service.ReviewService;
import com.chineselearning.flashcardservice.service.StudyStatsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/flashcards")
@Tag(name = "Flashcard Service", description = "Flashcard sets, cards and SM-2 reviews")
public class FlashcardController {

    private final FlashcardSetService setService;
    private final StudyStatsService statsService;
    private final ReviewService reviewService;

    public FlashcardController(FlashcardSetService setService,
                               StudyStatsService statsService,
                               ReviewService reviewService) {
        this.setService = setService;
        this.statsService = statsService;
        this.reviewService = reviewService;
    }

    // ---------- sets ----------

    @PostMapping("/sets")
    public ResponseEntity<FlashcardSetDto> createSet(@RequestHeader("X-User-Id") Long userId,
                                                     @Valid @RequestBody CreateFlashcardSetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(setService.createSet(userId, request));
    }

    @GetMapping("/sets/student/{studentId}")
    public List<FlashcardSetDto> getSetsByStudent(@RequestHeader("X-User-Id") Long userId,
                                                  @PathVariable Long studentId) {
        return setService.getSetsByStudent(userId, studentId);
    }

    @GetMapping("/sets/{setId}")
    public FlashcardSetDto getSet(@RequestHeader("X-User-Id") Long userId, @PathVariable Long setId) {
        return setService.getSet(userId, setId);
    }

    @PutMapping("/sets/{setId}")
    public FlashcardSetDto updateSet(@RequestHeader("X-User-Id") Long userId,
                                     @PathVariable Long setId,
                                     @Valid @RequestBody UpdateFlashcardSetRequest request) {
        return setService.updateSet(userId, setId, request);
    }

    @DeleteMapping("/sets/{setId}")
    public ResponseEntity<Void> deleteSet(@RequestHeader("X-User-Id") Long userId, @PathVariable Long setId) {
        setService.deleteSet(userId, setId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sets/{setId}/stats")
    public FlashcardSetStatsDto getSetStats(@RequestHeader("X-User-Id") Long userId, @PathVariable Long setId) {
        return statsService.getSetStats(userId, setId);
    }

    // ---------- cards ----------

    @PostMapping("/cards")
    public ResponseEntity<FlashcardDto> createCard(@RequestHeader("X-User-Id") Long userId,
                                                   @Valid @RequestBody CreateFlashcardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(setService.createCard(userId, request));
    }

    @GetMapping("/sets/{setId}/cards")
    public List<FlashcardDto> getCardsBySet(@RequestHeader("X-User-Id") Long userId, @PathVariable Long setId) {
        return setService.getCardsBySet(userId, setId);
    }

    @GetMapping("/cards/{flashcardId}")
    public FlashcardDto getCard(@RequestHeader("X-User-Id") Long userId, @PathVariable Long flashcardId) {
        return setService.getCard(userId, flashcardId);
    }

    @PutMapping("/cards/{flashcardId}")
    public FlashcardDto updateCard(@RequestHeader("X-User-Id") Long userId,
                                   @PathVariable Long flashcardId,
                                   @Valid @RequestBody UpdateFlashcardRequest request) {
        return setService.updateCard(userId, flashcardId, request);
    }

    @DeleteMapping("/cards/{flashcardId}")
    public ResponseEntity<Void> deleteCard(@RequestHeader("X-User-Id") Long userId, @PathVariable Long flashcardId) {
        setService.deleteCard(userId, flashcardId);
        return ResponseEntity.noContent().build();
    }

    // ---------- reviews (SM-2) ----------

    @PostMapping("/reviews")
    public ResponseEntity<ReviewResultDto> submitReview(@RequestHeader("X-User-Id") Long userId,
                                                        @Valid @RequestBody SubmitReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.submitReview(userId, request));
    }

    @GetMapping("/reviews/due/all")
    public TotalDueStatsDto getTotalDueStats(@RequestHeader("X-User-Id") Long userId) {
        return statsService.getTotalDueStats(userId);
    }

    @GetMapping("/reviews/due/{studentId}")
    public List<FlashcardProgressDto> getDueFlashcards(@RequestHeader("X-User-Id") Long userId,
                                                       @PathVariable Long studentId,
                                                       @RequestParam Long setId) {
        return reviewService.getDueFlashcards(userId, studentId, setId);
    }

    @GetMapping("/reviews/history/{studentId}/{flashcardId}")
    public List<FlashcardReviewDto> getReviewHistory(@RequestHeader("X-User-Id") Long userId,
                                                     @PathVariable Long studentId,
                                                     @PathVariable Long flashcardId) {
        return reviewService.getReviewHistory(userId, studentId, flashcardId);
    }

    @GetMapping("/reviews/progress/{studentId}/{flashcardId}")
    public FlashcardProgressDto getProgress(@RequestHeader("X-User-Id") Long userId,
                                            @PathVariable Long studentId,
                                            @PathVariable Long flashcardId) {
        return reviewService.getProgress(userId, studentId, flashcardId);
    }
}