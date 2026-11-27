package com.chineselearning.flashcardservice.repository;

import com.chineselearning.flashcardservice.entity.FlashcardSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlashcardSetRepository extends JpaRepository<FlashcardSet, Long> {

    List<FlashcardSet> findByStudentIdOrderByIdDesc(Long studentId);

    long countByStudentId(Long studentId);
}