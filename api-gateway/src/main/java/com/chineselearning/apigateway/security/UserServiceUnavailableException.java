package com.chineselearning.apigateway.security;

public class UserServiceUnavailableException extends RuntimeException
{
    public UserServiceUnavailableException(Throwable cause)
    {
        super("user-service is unavailable", cause);
    }
}