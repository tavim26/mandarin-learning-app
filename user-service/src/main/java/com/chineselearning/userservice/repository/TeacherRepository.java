package com.chineselearning.userservice.repository;

import com.chineselearning.userservice.entity.TeacherEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<TeacherEntity, Long>
{
}