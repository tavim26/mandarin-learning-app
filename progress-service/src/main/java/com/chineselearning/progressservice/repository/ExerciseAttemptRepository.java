package com.chineselearning.progressservice.repository;

import com.chineselearning.progressservice.entity.ExerciseAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ExerciseAttemptRepository extends JpaRepository<ExerciseAttempt, Long>
{

    int countByStudentIdAndExerciseId(Long studentId, Long exerciseId);

    List<ExerciseAttempt> findByStudentIdAndExerciseIdOrderByAttemptNumberAsc(Long studentId, Long exerciseId);

    @Query("""
            select count(distinct a.exerciseId) from ExerciseAttempt a
            where a.studentId = :studentId
              and a.exerciseId in :exerciseIds
              and a.isCorrect = true
            """)
    long countDistinctCorrectExercises(@Param("studentId") Long studentId,
                                       @Param("exerciseIds") Collection<Long> exerciseIds);
}