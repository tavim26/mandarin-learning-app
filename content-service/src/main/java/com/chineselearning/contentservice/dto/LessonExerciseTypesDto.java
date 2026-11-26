package com.chineselearning.contentservice.dto;

import java.util.Map;

public record LessonExerciseTypesDto(Long lessonId, Map<String, Long> exerciseTypes) {}