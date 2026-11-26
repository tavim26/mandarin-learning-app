package com.chineselearning.contentservice.exception;

/** The request is well-formed JSON but breaks a business rule; mapped to HTTP 400. */
public class InvalidRequestException extends RuntimeException
{
    public InvalidRequestException(String message)
    {
        super(message);
    }
}