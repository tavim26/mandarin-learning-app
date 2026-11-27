package com.chineselearning.progressservice.dto;

import java.math.BigDecimal;

public record StudentUnitProgressDto(
        Long unitId,
        Long studentId,
        int totalLessons,
        long completedLessons,
        long inProgressLessons,
        long notStartedLessons,
        BigDecimal unitCompletionPct
) {
}