package com.chineselearning.progressservice.exception;

public class ContentServiceUnavailableException extends RuntimeException {
    public ContentServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}