package com.chineselearning.chatbotservice.dto;

public record SendMessageResponse(ChatMessageDto userMessage, ChatMessageDto aiMessage) {
}