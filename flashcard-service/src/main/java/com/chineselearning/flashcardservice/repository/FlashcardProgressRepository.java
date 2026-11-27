package com.chineselearning.flashcardservice.repository;

import com.chineselearning.flashcardservice.entity.FlashcardProgress;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FlashcardProgressRepository extends JpaRepository<FlashcardProgress, Long> {

    Optional<FlashcardProgress> findByStudentIdAndFlashcardId(Long studentId, Long flashcardId);

    List<FlashcardProgress> findByStudentIdAndFlashcardIdIn(Long studentId, Collection<Long> flashcardIds);

    @Modifying
    @Query(value = """
            INSERT INTO flashcard_progress (student_id, flashcard_id, easiness_factor, interval_days, repetition_count)
            VALUES (:studentId, :flashcardId, 2.50, 0, 0)
            ON CONFLICT (student_id, flashcard_id) DO NOTHING
            """, nativeQuery = true)
    void insertIfAbsent(@Param("studentId") Long studentId, @Param("flashcardId") Long flashcardId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from FlashcardProgress p where p.studentId = :studentId and p.flashcard.id = :flashcardId")
    Optional<FlashcardProgress> findForUpdate(@Param("studentId") Long studentId,
                                              @Param("flashcardId") Long flashcardId);

    @Query("""
            select c.set.id as setId,
                   count(p) as seen,
                   count(case when p.nextReviewAt <= :now then 1 end) as due
            from FlashcardProgress p join p.flashcard c
            where p.studentId = :studentId and c.set.id in :setIds
            group by c.set.id
            """)
    List<SetProgressCounts> countProgressBySet(@Param("studentId") Long studentId,
                                               @Param("setIds") Collection<Long> setIds,
                                               @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from FlashcardProgress p where p.flashcard.id in (select c.id from Flashcard c where c.set.id = :setId)")
    void deleteAllBySetId(@Param("setId") Long setId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from FlashcardProgress p where p.flashcard.id = :flashcardId")
    void deleteAllByFlashcardId(@Param("flashcardId") Long flashcardId);
}