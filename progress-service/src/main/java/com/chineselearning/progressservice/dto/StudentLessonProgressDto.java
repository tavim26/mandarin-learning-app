package com.chineselearning.progressservice.dto;

import com.chineselearning.progressservice.entity.LessonStatus;
import com.chineselearning.progressservice.entity.StudentLessonProgress;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StudentLessonProgressDto(
        Long id,
        Long studentId,
        Long lessonId,
        LessonStatus status,
        BigDecimal completionPct,
        Integer xpAwarded,
        LocalDateTime startedAt,
        LocalDateTime lastAccessedAt,
        LocalDateTime completedAt
) {
    public static StudentLessonProgressDto from(StudentLessonProgress p) {
        return new StudentLessonProgressDto(p.getId(), p.getStudentId(), p.getLessonId(), p.getStatus(),
                p.getCompletionPct(), p.getXpAwarded(), p.getStartedAt(), p.getLastAccessedAt(), p.getCompletedAt());
    }
}