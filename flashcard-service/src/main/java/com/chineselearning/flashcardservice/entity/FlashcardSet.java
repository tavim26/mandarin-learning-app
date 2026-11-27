package com.chineselearning.flashcardservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "flashcard_sets", indexes = @Index(name = "idx_flashcard_sets_student_id", columnList = "student_id"))
public class FlashcardSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    protected FlashcardSet() {
    }

    public static FlashcardSet create(Long studentId, String title, String description) {
        FlashcardSet set = new FlashcardSet();
        set.studentId = studentId;
        set.title = title;
        set.description = description;
        return set;
    }

    public void update(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public boolean isOwnedBy(Long studentId) {
        return this.studentId.equals(studentId);
    }

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}