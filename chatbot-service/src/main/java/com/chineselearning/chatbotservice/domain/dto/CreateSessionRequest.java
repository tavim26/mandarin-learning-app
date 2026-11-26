package com.chineselearning.chatbotservice.domain.dto;

import jakarta.validation.constraints.Size;

public class CreateSessionRequest
{
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 1000, message = "Custom instructions must not exceed 1000 characters")
    private String customInstructions;

    public CreateSessionRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCustomInstructions() { return customInstructions; }
    public void setCustomInstructions(String customInstructions) { this.customInstructions = customInstructions; }
}