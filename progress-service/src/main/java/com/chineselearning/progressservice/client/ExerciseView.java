package com.chineselearning.progressservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExerciseView(Long id, Long lessonId, String type, Map<String, Object> contentData) {
}