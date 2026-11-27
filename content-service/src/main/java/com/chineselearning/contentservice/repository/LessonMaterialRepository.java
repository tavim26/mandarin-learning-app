package com.chineselearning.contentservice.repository;

import com.chineselearning.contentservice.entity.LessonMaterialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonMaterialRepository extends JpaRepository<LessonMaterialEntity, Long>
{
    List<LessonMaterialEntity> findByLessonIdOrderByIdAsc(Long lessonId);

    List<LessonMaterialEntity> findByLesson_Unit_Id(Long unitId);

    boolean existsByUrl(String url);

    @Query("select m from LessonMaterialEntity m join fetch m.lesson l join fetch l.unit where m.id = :id")
    Optional<LessonMaterialEntity> findWithUnitById(@Param("id") Long id);
}