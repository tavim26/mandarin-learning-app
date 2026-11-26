package com.chineselearning.contentservice.dto;

import com.chineselearning.contentservice.entity.LessonMaterialEntity;

public record LessonMaterialDto(Long id, Long lessonId, String title, String type, String url)
{
    public static LessonMaterialDto from(LessonMaterialEntity material)
    {
        return new LessonMaterialDto(material.getId(), material.getLesson().getId(),
                material.getTitle(), material.getType(), material.getUrl());
    }
}