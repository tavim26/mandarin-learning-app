package com.chineselearning.userservice.dto;

import com.chineselearning.userservice.entity.TeacherEntity;

public record TeacherDto(Long userId, String title)
{
    public static TeacherDto from(TeacherEntity teacher)
    {
        return new TeacherDto(teacher.getUserId(), teacher.getTitle());
    }
}