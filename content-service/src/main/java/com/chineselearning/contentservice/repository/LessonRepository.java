package com.chineselearning.contentservice.repository;

import com.chineselearning.contentservice.entity.LessonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<LessonEntity, Long>
{
    List<LessonEntity> findByUnitIdOrderByOrderIndexAsc(Long unitId);

    long countByUnitId(Long unitId);

    @Query("select coalesce(sum(l.xpReward), 0) from LessonEntity l where l.unit.id = :unitId")
    long sumXpRewardByUnitId(@Param("unitId") Long unitId);

    @Query("select l from LessonEntity l join fetch l.unit where l.id = :id")
    Optional<LessonEntity> findWithUnitById(@Param("id") Long id);
}