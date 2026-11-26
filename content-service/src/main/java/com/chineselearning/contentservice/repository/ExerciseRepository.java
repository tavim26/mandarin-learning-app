package com.chineselearning.contentservice.repository;

import com.chineselearning.contentservice.entity.ExerciseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExerciseRepository extends JpaRepository<ExerciseEntity, Long>
{
    List<ExerciseEntity> findByLessonIdOrderByIdAsc(Long lessonId);

    /** Loads the exercise with its lesson and unit, which is what ownership checks need. */
    @Query("select e from ExerciseEntity e join fetch e.lesson l join fetch l.unit where e.id = :id")
    Optional<ExerciseEntity> findWithUnitById(@Param("id") Long id);

    @Query("select e.type as exerciseType, count(e) as total from ExerciseEntity e " + "where e.lesson.id = :lessonId group by e.type")
    List<TypeCount> countByType(@Param("lessonId") Long lessonId);

    interface TypeCount
    {
        String getExerciseType();

        Long getTotal();
    }
}