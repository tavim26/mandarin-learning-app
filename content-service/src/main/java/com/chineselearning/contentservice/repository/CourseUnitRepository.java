package com.chineselearning.contentservice.repository;

import com.chineselearning.contentservice.entity.CourseUnitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseUnitRepository extends JpaRepository<CourseUnitEntity, Long>
{
    List<CourseUnitEntity> findAllByOrderByOrderIndexAsc();

    List<CourseUnitEntity> findByHskLevelOrderByOrderIndexAsc(Integer hskLevel);

    List<CourseUnitEntity> findByCreatedByTeacherIdOrderByOrderIndexAsc(Long teacherId);
}