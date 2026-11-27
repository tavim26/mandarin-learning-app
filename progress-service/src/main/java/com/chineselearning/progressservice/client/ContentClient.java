package com.chineselearning.progressservice.client;

import com.chineselearning.progressservice.config.ContentServiceProperties;
import com.chineselearning.progressservice.exception.ContentServiceUnavailableException;
import com.chineselearning.progressservice.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.function.Supplier;

@Component
public class ContentClient {

    private static final Logger log = LoggerFactory.getLogger(ContentClient.class);
    private static final int MAX_ATTEMPTS = 2;

    private final RestClient restClient;

    public ContentClient(ContentServiceProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) properties.connectTimeout().toMillis());
        requestFactory.setReadTimeout((int) properties.readTimeout().toMillis());
        this.restClient = RestClient.builder()
                .baseUrl(properties.url())
                .requestFactory(requestFactory)
                .build();
    }

    public ExerciseView getExercise(Long exerciseId)
    {
        ExerciseView exercise = fetch("exercise " + exerciseId, () -> restClient.get()
                .uri("/api/content/exercises/{id}", exerciseId)
                .retrieve()
                .body(ExerciseView.class));

        if (exercise == null)
        {
            throw new ResourceNotFoundException("Exercise " + exerciseId + " not found");
        }
        return exercise;
    }

    public LessonView getLesson(Long lessonId)
    {
        LessonView lesson = fetch("lesson " + lessonId, () -> restClient.get()
                .uri("/api/content/lessons/{id}", lessonId)
                .retrieve()
                .body(LessonView.class));

        if (lesson == null)
        {
            throw new ResourceNotFoundException("Lesson " + lessonId + " not found");
        }
        return lesson;
    }

    public List<LessonView> getLessonsForUnit(Long unitId)
    {
        List<LessonView> lessons = fetch("unit " + unitId, () -> restClient.get()
                .uri("/api/content/units/{id}/lessons", unitId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<LessonView>>() {
                }));

        return lessons == null ? List.of() : lessons;
    }


    //helper

    private <T> T fetch(String what, Supplier<T> call)
    {
        RuntimeException last = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++)
        {
            try {
                return call.get();
            } catch (HttpClientErrorException e) {

                if (e.getStatusCode() == HttpStatus.NOT_FOUND)
                {
                    throw new ResourceNotFoundException(what + " not found");
                }
                throw new ContentServiceUnavailableException("content-service rejected request for " + what
                        + " with status " + e.getStatusCode().value(), e);

            } catch (HttpServerErrorException | ResourceAccessException e) {
                log.warn("content-service call failed for {} (attempt {}/{}): {}",
                        what, attempt, MAX_ATTEMPTS, e.getMessage());
                last = e;
            }
        }
        throw new ContentServiceUnavailableException("content-service unreachable for " + what, last);
    }
}