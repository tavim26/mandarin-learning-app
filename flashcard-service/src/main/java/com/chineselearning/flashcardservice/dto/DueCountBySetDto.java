package com.chineselearning.flashcardservice.dto;

public record DueCountBySetDto(Long setId, String setTitle, Integer dueCount) {
}