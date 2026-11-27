package com.chineselearning.chatbotservice.exception;

public class SessionClosedException extends RuntimeException {
    public SessionClosedException() {
        super("This chat session has ended. No more messages can be sent");
    }
}