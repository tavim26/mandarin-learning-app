package com.chineselearning.chatbotservice.exception;

public class SessionNotFoundException extends RuntimeException {
    public SessionNotFoundException(Long sessionId) {
        super("Chat session " + sessionId + " not found");
    }
}