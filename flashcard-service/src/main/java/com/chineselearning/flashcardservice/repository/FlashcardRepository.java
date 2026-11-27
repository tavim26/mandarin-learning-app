package com.chineselearning.flashcardservice.repository;

import com.chineselearning.flashcardservice.entity.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {

    List<Flashcard> findBySetIdOrderByIdAsc(Long setId);

    long countBySetId(Long setId);

    @Query("select c from Flashcard c join fetch c.set where c.id = :id")
    Optional<Flashcard> findWithSetById(@Param("id") Long id);

    @Query("select c.id from Flashcard c where c.set.id = :setId order by c.id")
    List<Long> findIdsBySetId(@Param("setId") Long setId);

    @Query("""
            select c.set.id as setId, count(c) as total
            from Flashcard c
            where c.set.id in :setIds
            group by c.set.id
            """)
    List<SetCardCount> countBySetIds(@Param("setIds") Collection<Long> setIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Flashcard c where c.set.id = :setId")
    void deleteAllBySetId(@Param("setId") Long setId);
}