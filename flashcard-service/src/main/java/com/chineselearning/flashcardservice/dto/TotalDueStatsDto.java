package com.chineselearning.flashcardservice.dto;

import java.util.List;

public record TotalDueStatsDto(Integer totalDue, List<DueCountBySetDto> bySet) {
}