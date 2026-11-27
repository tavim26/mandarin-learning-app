package com.chineselearning.progressservice.repository;

import com.chineselearning.progressservice.entity.LessonStatus;
import com.chineselearning.progressservice.entity.StudentLessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StudentLessonProgressRepository extends JpaRepository<StudentLessonProgress, Long> {

    Optional<StudentLessonProgress> findByStudentIdAndLessonId(Long studentId, Long lessonId);

    List<StudentLessonProgress> findByStudentIdOrderByLessonIdAsc(Long studentId);

    List<StudentLessonProgress> findByStudentIdAndStatusOrderByLessonIdAsc(Long studentId, LessonStatus status);

    List<StudentLessonProgress> findTop10ByLessonIdOrderByCompletionPctDescIdAsc(Long lessonId);

    long countByStudentIdAndStatus(Long studentId, LessonStatus status);

    List<StudentLessonProgress> findByStudentIdAndLessonIdIn(Long studentId, Collection<Long> lessonIds);
}