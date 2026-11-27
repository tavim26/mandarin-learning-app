package com.chineselearning.chatbotservice.dto;

import com.chineselearning.chatbotservice.entity.ChatMessage;

import java.time.LocalDateTime;

public record ChatMessageDto(
        Long id,
        Long sessionId,
        String sender,
        String content,
        LocalDateTime createdAt
) {
    public static ChatMessageDto from(ChatMessage message) {
        return new ChatMessageDto(
                message.getId(),
                message.getSession().getId(),
                message.getSender().name(),
                message.getContent(),
                message.getCreatedAt());
    }
}