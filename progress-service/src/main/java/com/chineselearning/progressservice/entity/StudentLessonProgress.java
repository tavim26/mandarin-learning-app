package com.chineselearning.progressservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_lesson_progress",
        uniqueConstraints = @UniqueConstraint(name = "uq_student_lesson", columnNames = {"student_id", "lesson_id"}),
        indexes = @Index(name = "idx_progress_student_status", columnList = "student_id, status"))
public class StudentLessonProgress {

    private static final BigDecimal FULL = new BigDecimal("100");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LessonStatus status;

    @Column(name = "completion_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal completionPct;

    @Column(name = "xp_awarded")
    private Integer xpAwarded;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    protected StudentLessonProgress() {
    }

    public StudentLessonProgress(Long studentId, Long lessonId) {
        this.studentId = studentId;
        this.lessonId = lessonId;
        this.status = LessonStatus.NOT_STARTED;
        this.completionPct = BigDecimal.ZERO;
    }


    public boolean updateCompletion(BigDecimal newPct, LocalDateTime now)
    {
        if (startedAt == null)
        {
            startedAt = now;
        }

        completionPct = newPct;
        lastAccessedAt = now;

        if (newPct.compareTo(FULL) == 0)
        {
            if (status != LessonStatus.COMPLETED)
            {
                status = LessonStatus.COMPLETED;
                completedAt = now;
                return xpAwarded == null || xpAwarded == 0;
            }
            return false;
        }
        if (newPct.signum() > 0)
        {
            if (status != LessonStatus.IN_PROGRESS)
            {
                status = LessonStatus.IN_PROGRESS;
                completedAt = null;
            }
            return false;
        }

        status = LessonStatus.NOT_STARTED;
        completedAt = null;
        return false;
    }

    public void grantXp(int xp) {
        this.xpAwarded = xp;
    }

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public Long getLessonId() {
        return lessonId;
    }

    public LessonStatus getStatus() {
        return status;
    }

    public BigDecimal getCompletionPct() {
        return completionPct;
    }

    public Integer getXpAwarded() {
        return xpAwarded;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}