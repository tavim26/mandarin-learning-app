package com.chineselearning.chatbotservice.dto;

import com.chineselearning.chatbotservice.entity.ChatSession;

import java.time.LocalDateTime;

public record ChatSessionDto(
        Long id,
        Long studentId,
        String title,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        String lastMessagePreview,
        String customInstructions,
        long messageCount
) {
    private static final int PREVIEW_LENGTH = 60;

    public static ChatSessionDto from(ChatSession session, String lastMessageContent, long messageCount) {
        return new ChatSessionDto(
                session.getId(),
                session.getStudentId(),
                session.getTitle(),
                session.getStartedAt(),
                session.getEndedAt(),
                preview(lastMessageContent),
                session.getCustomInstructions(),
                messageCount);
    }

    private static String preview(String content) {
        if (content == null) {
            return null;
        }
        return content.length() <= PREVIEW_LENGTH
                ? content
                : content.substring(0, PREVIEW_LENGTH) + "...";
    }
}