package com.chineselearning.chatbotservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

final class GeminiModels {

    private GeminiModels() {
    }

    record Part(String text) {
    }

    record Content(String role, List<Part> parts) {
    }

    record GenerationConfig(int maxOutputTokens, double temperature) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record GenerateRequest(
            @JsonProperty("system_instruction") Content systemInstruction,
            List<Content> contents,
            GenerationConfig generationConfig
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GenerateResponse(List<Candidate> candidates, PromptFeedback promptFeedback) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Candidate(Content content, String finishReason) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PromptFeedback(String blockReason) {
    }
}