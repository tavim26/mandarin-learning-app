package com.chineselearning.chatbotservice.client;

import com.chineselearning.chatbotservice.client.GeminiModels.Candidate;
import com.chineselearning.chatbotservice.client.GeminiModels.Content;
import com.chineselearning.chatbotservice.client.GeminiModels.GenerateRequest;
import com.chineselearning.chatbotservice.client.GeminiModels.GenerateResponse;
import com.chineselearning.chatbotservice.client.GeminiModels.GenerationConfig;
import com.chineselearning.chatbotservice.client.GeminiModels.Part;
import com.chineselearning.chatbotservice.config.GeminiProperties;
import com.chineselearning.chatbotservice.exception.AiRefusedException;
import com.chineselearning.chatbotservice.exception.AiUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);
    private static final long BACKOFF_MILLIS = 500;

    public record Turn(String role, String text) {
    }

    private final RestClient restClient;
    private final GeminiProperties properties;

    public GeminiClient(GeminiProperties properties)
    {
        this.properties = properties;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) properties.connectTimeout().toMillis());
        requestFactory.setReadTimeout((int) properties.readTimeout().toMillis());
        this.restClient = RestClient.builder()
                .baseUrl(properties.api().baseUrl())
                .requestFactory(requestFactory)
                .defaultHeader("x-goog-api-key", properties.api().key())
                .build();
    }

    public String generate(String systemPrompt, List<Turn> turns)
    {
        GenerateRequest request = new GenerateRequest(
                new Content(null, List.of(new Part(systemPrompt))),
                turns.stream()
                        .map(t -> new Content(t.role(), List.of(new Part(t.text()))))
                        .toList(),
                new GenerationConfig(properties.maxOutputTokens(), 0.7));

        GenerateResponse response = callWithRetry(request);
        return extractText(response);
    }

    private GenerateResponse callWithRetry(GenerateRequest request)
    {
        RuntimeException last = null;

        for (int attempt = 1; attempt <= properties.maxAttempts(); attempt++)
        {
            try {
                return restClient.post()
                        .uri("/models/{model}:generateContent", properties.model())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(GenerateResponse.class);

            } catch (HttpStatusCodeException e) {

                int status = e.getStatusCode().value();
                boolean retryable = e instanceof HttpServerErrorException || status == 429;
                log.warn("Gemini call failed (attempt {}/{}): status {}", attempt, properties.maxAttempts(), status);
                last = e;

                if (!retryable)
                {
                    break;
                }
            } catch (ResourceAccessException e) {
                log.warn("Gemini call failed (attempt {}/{}): {}", attempt, properties.maxAttempts(), e.getMessage());
                last = e;
            }
            if (attempt < properties.maxAttempts()) {
                sleep(BACKOFF_MILLIS * attempt);
            }
        }
        throw new AiUnavailableException("Gemini request failed", last);
    }


    private String extractText(GenerateResponse response)
    {
        if (response == null)
        {
            throw new AiUnavailableException("Gemini returned an empty body", null);
        }

        if (response.promptFeedback() != null && response.promptFeedback().blockReason() != null)
        {
            throw new AiRefusedException("Prompt blocked: " + response.promptFeedback().blockReason());
        }

        if (response.candidates() == null || response.candidates().isEmpty())
        {
            throw new AiRefusedException("No candidates returned");
        }

        Candidate candidate = response.candidates().get(0);
        String text = candidate.content() == null || candidate.content().parts() == null
                ? ""
                : candidate.content().parts().stream()
                .map(Part::text)
                .filter(t -> t != null && !t.isBlank())
                .collect(Collectors.joining());

        if (text.isBlank())
        {
            throw new AiRefusedException("Empty answer, finishReason=" + candidate.finishReason());
        }
        return text;
    }

    private static void sleep(long millis)
    {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}