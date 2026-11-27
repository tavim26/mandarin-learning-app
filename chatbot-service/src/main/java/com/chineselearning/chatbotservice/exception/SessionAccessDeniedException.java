package com.chineselearning.chatbotservice.exception;

public class SessionAccessDeniedException extends RuntimeException {
    public SessionAccessDeniedException() {
        super("You do not have access to this chat session");
    }
}