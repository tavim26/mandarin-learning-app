package com.chineselearning.contentservice.dto;

import com.chineselearning.contentservice.entity.LessonEntity;

import java.util.List;

public record LessonDto(Long id, Long unitId, String title, String description, Integer xpReward, Integer orderIndex, List<ExerciseDto> exercises)
{

    public static LessonDto from(LessonEntity lesson)
    {
        return new LessonDto(lesson.getId(), lesson.getUnit().getId(), lesson.getTitle(),
                lesson.getDescription(), lesson.getXpReward(), lesson.getOrderIndex(), null);
    }

    public LessonDto withExercises(List<ExerciseDto> exercises)
    {
        return new LessonDto(id, unitId, title, description, xpReward, orderIndex, exercises);
    }
}