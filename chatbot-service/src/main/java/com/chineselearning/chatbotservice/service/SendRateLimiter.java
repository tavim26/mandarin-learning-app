package com.chineselearning.chatbotservice.service;

import com.chineselearning.chatbotservice.config.ChatbotProperties;
import com.chineselearning.chatbotservice.exception.RateLimitExceededException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class SendRateLimiter {

    private final Map<Long, Deque<Long>> hitsByStudent = new ConcurrentHashMap<>();
    private final int maxMessages;
    private final long windowNanos;

    public SendRateLimiter(ChatbotProperties properties) {
        this.maxMessages = properties.rateLimit().maxMessages();
        this.windowNanos = properties.rateLimit().window().toNanos();
    }

    public void check(Long studentId)
    {
        check(studentId, System.nanoTime());
    }

    void check(Long studentId, long nowNanos)
    {
        Deque<Long> hits = hitsByStudent.computeIfAbsent(studentId, id -> new ArrayDeque<>());

        synchronized (hits) {

            while (!hits.isEmpty() && nowNanos - hits.peekFirst() >= windowNanos)
            {
                hits.pollFirst();
            }
            if (hits.size() >= maxMessages)
            {
                long waitNanos = windowNanos - (nowNanos - hits.peekFirst());
                long retryAfter = Math.max(1, Duration.ofNanos(waitNanos).toSeconds() + 1);
                throw new RateLimitExceededException(retryAfter);
            }
            hits.addLast(nowNanos);
        }
    }
}