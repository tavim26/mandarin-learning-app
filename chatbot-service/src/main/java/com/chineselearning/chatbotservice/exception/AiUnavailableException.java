package com.chineselearning.chatbotservice.exception;

/** The AI provider is unreachable or failing. Mapped to 503 without leaking upstream details. */
public class AiUnavailableException extends RuntimeException {
    public AiUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}