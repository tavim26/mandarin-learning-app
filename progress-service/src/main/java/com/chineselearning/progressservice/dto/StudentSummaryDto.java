package com.chineselearning.progressservice.dto;

public record StudentSummaryDto(
        Long studentId,
        Integer xpTotal,
        Integer level,
        long completedLessonsCount,
        long inProgressLessonsCount
) {
}