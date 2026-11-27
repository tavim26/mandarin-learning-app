package com.chineselearning.progressservice.dto;

import com.chineselearning.progressservice.entity.ExerciseAttempt;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public record ExerciseAttemptDto(
        Long id,
        Long studentId,
        Long exerciseId,
        Integer attemptNumber,
        LocalDateTime submittedAt,
        Map<String, Object> submittedAnswer,
        Boolean isCorrect,
        BigDecimal score,
        String feedbackText
) {
    public static ExerciseAttemptDto from(ExerciseAttempt a) {
        return new ExerciseAttemptDto(a.getId(), a.getStudentId(), a.getExerciseId(), a.getAttemptNumber(),
                a.getSubmittedAt(), a.getSubmittedAnswer(), a.getIsCorrect(), a.getScore(), a.getFeedbackText());
    }
}