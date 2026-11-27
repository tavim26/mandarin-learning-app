package com.chineselearning.chatbotservice.service;

import com.chineselearning.chatbotservice.config.ChatbotProperties;
import com.chineselearning.chatbotservice.exception.RateLimitExceededException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SendRateLimiterTest {

    private final SendRateLimiter limiter = new SendRateLimiter(
            new ChatbotProperties(new ChatbotProperties.Context(20),
                    new ChatbotProperties.RateLimit(3, Duration.ofSeconds(60))));

    private static final long SECOND = 1_000_000_000L;

    @Test
    void allowsUpToTheLimitThenRejects()
    {
        assertDoesNotThrow(() -> limiter.check(1L, 0));
        assertDoesNotThrow(() -> limiter.check(1L, SECOND));
        assertDoesNotThrow(() -> limiter.check(1L, 2 * SECOND));
        RateLimitExceededException e =
                assertThrows(RateLimitExceededException.class, () -> limiter.check(1L, 3 * SECOND));
        org.junit.jupiter.api.Assertions.assertTrue(e.getRetryAfterSeconds() >= 1);
    }

    @Test
    void windowSlidesAndFreesCapacity()
    {
        limiter.check(1L, 0);
        limiter.check(1L, SECOND);
        limiter.check(1L, 2 * SECOND);
        assertDoesNotThrow(() -> limiter.check(1L, 61 * SECOND));
    }

    @Test
    void studentsAreIndependent()
    {
        limiter.check(1L, 0);
        limiter.check(1L, 0);
        limiter.check(1L, 0);
        assertDoesNotThrow(() -> limiter.check(2L, 0));
    }
}