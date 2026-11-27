package com.chineselearning.flashcardservice.entity;

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

import java.time.LocalDateTime;

@Entity
@Table(name = "flashcard_reviews", indexes = {
        @Index(name = "idx_reviews_student_id", columnList = "student_id"),
        @Index(name = "idx_reviews_student_flashcard", columnList = "student_id, flashcard_id")
})
public class FlashcardReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    @Column(name = "reviewed_at", nullable = false)
    private LocalDateTime reviewedAt;

    @Column(name = "quality", nullable = false)
    private Integer quality;

    protected FlashcardReview() {
    }

    public static FlashcardReview of(Long studentId, Flashcard flashcard, int quality, LocalDateTime reviewedAt) {
        FlashcardReview review = new FlashcardReview();
        review.studentId = studentId;
        review.flashcard = flashcard;
        review.quality = quality;
        review.reviewedAt = reviewedAt;
        return review;
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

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public Integer getQuality() {
        return quality;
    }
}