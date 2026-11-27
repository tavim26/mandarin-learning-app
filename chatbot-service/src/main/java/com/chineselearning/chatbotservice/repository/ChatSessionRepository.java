package com.chineselearning.chatbotservice.repository;

import com.chineselearning.chatbotservice.entity.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    Optional<ChatSession> findByIdAndStudentId(Long id, Long studentId);

    List<ChatSession> findByStudentIdOrderByStartedAtDescIdDesc(Long studentId);

    Page<ChatSession> findByStudentIdOrderByStartedAtDescIdDesc(Long studentId, Pageable pageable);
}