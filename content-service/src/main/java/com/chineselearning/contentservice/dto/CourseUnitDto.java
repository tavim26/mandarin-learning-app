package com.chineselearning.contentservice.dto;

import com.chineselearning.contentservice.entity.CourseUnitEntity;

public record CourseUnitDto(Long id, String title, String description, Integer hskLevel, Integer orderIndex, Long createdByTeacherId)
{
    public static CourseUnitDto from(CourseUnitEntity unit)
    {
        return new CourseUnitDto(unit.getId(), unit.getTitle(), unit.getDescription(),
                unit.getHskLevel(), unit.getOrderIndex(), unit.getCreatedByTeacherId());
    }
}