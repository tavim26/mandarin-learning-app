package com.chineselearning.chatbotservice.repository;

import com.chineselearning.chatbotservice.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionIdOrderByCreatedAtAscIdAsc(Long sessionId);

    Page<ChatMessage> findBySessionIdOrderByCreatedAtAscIdAsc(Long sessionId, Pageable pageable);

    /** Latest messages first; the service reverses them to chronological order. */
    List<ChatMessage> findBySessionIdOrderByIdDesc(Long sessionId, Pageable pageable);

    long countBySessionId(Long sessionId);

    /** One query for any number of sessions: removes the N+1 of the old implementation. */
    @Query("""
            select m.session.id as sessionId,
                   count(m) as messageCount,
                   max(m.id) as lastMessageId
            from ChatMessage m
            where m.session.id in :sessionIds
            group by m.session.id
            """)
    List<SessionMessageStats> findStatsBySessionIds(@Param("sessionIds") Collection<Long> sessionIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from ChatMessage m where m.session.id = :sessionId")
    void deleteAllBySessionId(@Param("sessionId") Long sessionId);
}