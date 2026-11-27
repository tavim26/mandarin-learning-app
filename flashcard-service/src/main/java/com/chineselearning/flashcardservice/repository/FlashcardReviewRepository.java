package com.chineselearning.flashcardservice.repository;

import com.chineselearning.flashcardservice.entity.FlashcardReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FlashcardReviewRepository extends JpaRepository<FlashcardReview, Long> {

    List<FlashcardReview> findByStudentIdAndFlashcardIdOrderByReviewedAtAscIdAsc(Long studentId, Long flashcardId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from FlashcardReview r where r.flashcard.id in (select c.id from Flashcard c where c.set.id = :setId)")
    void deleteAllBySetId(@Param("setId") Long setId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from FlashcardReview r where r.flashcard.id = :flashcardId")
    void deleteAllByFlashcardId(@Param("flashcardId") Long flashcardId);
}