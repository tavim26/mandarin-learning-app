package com.chineselearning.userservice.exception;

public class ConflictException extends RuntimeException
{
    public ConflictException(String message)
    {
        super(message);
    }
}