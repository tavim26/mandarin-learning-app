package com.chineselearning.chatbotservice.exception;

public class AiRefusedException extends RuntimeException {
    public AiRefusedException(String message) {
        super(message);
    }
}