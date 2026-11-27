package com.chineselearning.progressservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LessonView(Long id, Integer xpReward, List<ExerciseView> exercises) {
}