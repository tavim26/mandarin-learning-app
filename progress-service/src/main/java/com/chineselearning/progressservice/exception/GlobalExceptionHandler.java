package com.chineselearning.progressservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler
{

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(ResourceNotFoundException e)
    {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    ProblemDetail handleForbidden(ForbiddenOperationException e)
    {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(InvalidAnswerException.class)
    ProblemDetail handleInvalidAnswer(InvalidAnswerException e)
    {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(ExerciseConfigurationException.class)
    ProblemDetail handleBadExercise(ExerciseConfigurationException e)
    {
        log.error("Malformed exercise in content-service: {}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "This exercise is misconfigured. Please report it to your teacher");
    }

    @ExceptionHandler(ContentServiceUnavailableException.class)
    ProblemDetail handleContentUnavailable(ContentServiceUnavailableException e)
    {
        log.warn("content-service unavailable: {}", e.getMessage(), e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "A required service is temporarily unavailable. Please try again later");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception e)
    {
        log.error("Unexpected error", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
    }
}