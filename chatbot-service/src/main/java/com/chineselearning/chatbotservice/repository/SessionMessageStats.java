package com.chineselearning.chatbotservice.repository;

public interface SessionMessageStats {

    Long getSessionId();

    long getMessageCount();

    Long getLastMessageId();
}