package com.chineselearning.contentservice.dto;

import com.chineselearning.contentservice.entity.ExerciseEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record ExerciseDto(Long id, Long lessonId, String type, String prompt,
                          Integer difficulty, Map<String, Object> contentData)
{
    private static final Map<String, Set<String>> ANSWER_KEYS = Map.of(
            "MULTIPLE_CHOICE", Set.of("correctIndex"),
            "TRANSLATION", Set.of("acceptedAnswers"),
            "FILL_BLANK", Set.of("correctAnswers"),
            "ORDERING", Set.of("correctOrder"),
            "MATCHING", Set.of("pairs"));

    public static ExerciseDto from(ExerciseEntity exercise)
    {
        return new ExerciseDto(exercise.getId(), exercise.getLesson().getId(), exercise.getType(),
                exercise.getPrompt(), exercise.getDifficulty(), exercise.getContentData());
    }

    public ExerciseDto withoutAnswers()
    {
        if (contentData == null)
        {
            return this;
        }
        Map<String, Object> visible = new LinkedHashMap<>(contentData);
        ANSWER_KEYS.getOrDefault(type, Set.of()).forEach(visible::remove);

        if ("ORDERING".equals(type) && contentData.get("words") instanceof List<?> words)
        {
            List<Object> shuffled = new ArrayList<>(words);
            Collections.shuffle(shuffled);
            visible.put("words", shuffled);
        }
        if ("MATCHING".equals(type) && contentData.get("pairs") instanceof List<?> pairs)
        {
            List<Object> left = new ArrayList<>();
            List<Object> right = new ArrayList<>();
            for (Object pair : pairs)
            {
                if (pair instanceof Map<?, ?> entry)
                {
                    left.add(entry.get("left"));
                    right.add(entry.get("right"));
                }
            }
            Collections.shuffle(right);
            visible.put("leftItems", left);
            visible.put("rightItems", right);
        }
        return new ExerciseDto(id, lessonId, type, prompt, difficulty, visible);
    }
}