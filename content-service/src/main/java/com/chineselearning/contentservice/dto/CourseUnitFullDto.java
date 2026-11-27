package com.chineselearning.contentservice.dto;

import com.chineselearning.contentservice.entity.CourseUnitEntity;

import java.util.List;

public record CourseUnitFullDto(Long id, String title, String description, Integer hskLevel, Integer orderIndex, List<LessonDto> lessons)
{
    public static CourseUnitFullDto from(CourseUnitEntity unit, List<LessonDto> lessons)
    {
        return new CourseUnitFullDto(unit.getId(), unit.getTitle(), unit.getDescription(),
                unit.getHskLevel(), unit.getOrderIndex(), lessons);
    }
}