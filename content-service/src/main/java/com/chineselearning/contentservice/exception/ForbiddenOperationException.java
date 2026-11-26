package com.chineselearning.contentservice.exception;

public class ForbiddenOperationException extends RuntimeException
{
    public ForbiddenOperationException(String message)
    {
        super(message);
    }
}