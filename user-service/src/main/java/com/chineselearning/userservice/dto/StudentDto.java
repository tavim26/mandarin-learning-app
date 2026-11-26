package com.chineselearning.userservice.dto;

import com.chineselearning.userservice.entity.StudentEntity;

public record StudentDto(Long userId, String nickname)
{
    public static StudentDto from(StudentEntity student)
    {
        return new StudentDto(student.getUserId(), student.getNickname());
    }
}