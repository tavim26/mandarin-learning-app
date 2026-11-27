package com.chineselearning.flashcardservice.entity;

import com.chineselearning.flashcardservice.scheduling.Sm2Scheduler;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "flashcard_progress", indexes = {
        @Index(name = "idx_progress_student_flashcard", columnList = "student_id, flashcard_id", unique = true),
        @Index(name = "idx_progress_student_next_review", columnList = "student_id, next_review_at")
})
public class FlashcardProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    @Column(name = "easiness_factor", nullable = false, precision = 4, scale = 2)
    private BigDecimal easinessFactor;

    @Column(name = "interval_days", nullable = false)
    private Integer intervalDays;

    @Column(name = "repetition_count", nullable = false)
    private Integer repetitionCount;

    @Column(name = "next_review_at")
    private LocalDateTime nextReviewAt;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    protected FlashcardProgress() {
    }

    /** Rows are created by FlashcardProgressRepository.insertIfAbsent (race-free), never by constructor. */
    public void apply(Sm2Scheduler.Result result, LocalDateTime reviewedAt) {
        this.easinessFactor = result.easinessFactor();
        this.intervalDays = result.intervalDays();
        this.repetitionCount = result.repetitionCount();
        this.nextReviewAt = result.nextReviewAt();
        this.lastReviewedAt = reviewedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public Flashcard getFlashcard() {
        return flashcard;
    }

    public BigDecimal getEasinessFactor() {
        return easinessFactor;
    }

    public Integer getIntervalDays() {
        return intervalDays;
    }

    public Integer getRepetitionCount() {
        return repetitionCount;
    }

    public LocalDateTime getNextReviewAt() {
        return nextReviewAt;
    }

    public LocalDateTime getLastReviewedAt() {
        return lastReviewedAt;
    }
}