package com.chineselearning.contentservice.dto;

import com.chineselearning.contentservice.entity.ExerciseEntity;

import java.util.Map;

public record ExerciseDto(Long id, Long lessonId, String type, String prompt,
                          Integer difficulty, Map<String, Object> contentData)
{
    public static ExerciseDto from(ExerciseEntity exercise)
    {
        return new ExerciseDto(exercise.getId(), exercise.getLesson().getId(), exercise.getType(),
                exercise.getPrompt(), exercise.getDifficulty(), exercise.getContentData());
    }
}