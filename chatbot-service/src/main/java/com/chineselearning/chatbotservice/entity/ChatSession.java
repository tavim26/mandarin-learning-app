package com.chineselearning.chatbotservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_sessions", indexes = @Index(name = "idx_chat_sessions_student_id", columnList = "student_id"))
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    private String title;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "custom_instructions", columnDefinition = "TEXT")
    private String customInstructions;

    protected ChatSession() {
    }

    public static ChatSession start(Long studentId, String title, String customInstructions) {
        ChatSession session = new ChatSession();
        session.studentId = studentId;
        session.title = title;
        session.customInstructions = customInstructions;
        session.startedAt = LocalDateTime.now();
        return session;
    }

    public boolean isOwnedBy(Long studentId) {
        return this.studentId.equals(studentId);
    }

    public boolean isEnded() {
        return endedAt != null;
    }

    public void end() {
        if (endedAt == null) {
            endedAt = LocalDateTime.now();
        }
    }

    public void rename(String newTitle) {
        this.title = newTitle;
    }

    public boolean hasTitle() {
        return title != null && !title.isBlank();
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

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public String getCustomInstructions() {
        return customInstructions;
    }
}