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

@Entity
@Table(name = "flashcards", indexes = @Index(name = "idx_flashcards_set_id", columnList = "set_id"))
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "set_id", nullable = false)
    private FlashcardSet set;

    @Column(name = "front_text", columnDefinition = "TEXT", nullable = false)
    private String frontText;

    @Column(name = "back_text", columnDefinition = "TEXT", nullable = false)
    private String backText;

    protected Flashcard() {
    }

    public static Flashcard create(FlashcardSet set, String frontText, String backText) {
        Flashcard card = new Flashcard();
        card.set = set;
        card.frontText = frontText;
        card.backText = backText;
        return card;
    }

    public void update(String frontText, String backText) {
        this.frontText = frontText;
        this.backText = backText;
    }

    public Long getId() {
        return id;
    }

    public FlashcardSet getSet() {
        return set;
    }

    public String getFrontText() {
        return frontText;
    }

    public String getBackText() {
        return backText;
    }
}