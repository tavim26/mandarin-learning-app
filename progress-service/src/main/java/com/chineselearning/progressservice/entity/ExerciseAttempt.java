package com.chineselearning.progressservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "exercise_attempts",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_attempt_student_exercise_number",
                columnNames = {"student_id", "exercise_id", "attempt_number"}),
        indexes = {
                @Index(name = "idx_attempts_student_exercise", columnList = "student_id, exercise_id, submitted_at"),
                @Index(name = "idx_attempts_exercise_correct", columnList = "exercise_id, is_correct")
        })
public class ExerciseAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "exercise_id", nullable = false)
    private Long exerciseId;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "submitted_answer", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> submittedAnswer;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    @Column(name = "score", nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "feedback_text", columnDefinition = "TEXT")
    private String feedbackText;

    protected ExerciseAttempt() {
    }

    public static ExerciseAttempt record(Long studentId, Long exerciseId, int attemptNumber,
                                         Map<String, Object> submittedAnswer,
                                         boolean correct, BigDecimal score, String feedback) {
        ExerciseAttempt attempt = new ExerciseAttempt();
        attempt.studentId = studentId;
        attempt.exerciseId = exerciseId;
        attempt.attemptNumber = attemptNumber;
        attempt.submittedAt = LocalDateTime.now();
        attempt.submittedAnswer = submittedAnswer;
        attempt.isCorrect = correct;
        attempt.score = score;
        attempt.feedbackText = feedback;
        return attempt;
    }

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public Long getExerciseId() {
        return exerciseId;
    }

    public Integer getAttemptNumber() {
        return attemptNumber;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public Map<String, Object> getSubmittedAnswer() {
        return submittedAnswer;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public BigDecimal getScore() {
        return score;
    }

    public String getFeedbackText() {
        return feedbackText;
    }
}