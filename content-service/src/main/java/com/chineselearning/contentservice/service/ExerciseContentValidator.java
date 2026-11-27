package com.chineselearning.contentservice.service;

import com.chineselearning.contentservice.exception.InvalidRequestException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Checks that the contentData of an exercise has exactly the shape the evaluator in
 * progress-service reads, so an exercise that can never be answered correctly cannot be saved.
 */
public final class ExerciseContentValidator
{
    private static final int MAX_ITEMS = 50;
    private static final int MAX_TEXT_LENGTH = 500;

    private ExerciseContentValidator() {}

    public static void validate(String type, Map<String, Object> data)
    {
        if (type == null || data == null)
        {
            throw invalid("Exercise type and content are mandatory");
        }

        switch (type)
        {
            case "MULTIPLE_CHOICE" -> validateMultipleChoice(data);
            case "TRANSLATION" -> texts(data, "acceptedAnswers", 1);
            case "FILL_BLANK" -> texts(data, "correctAnswers", 1);
            case "MATCHING" -> validateMatching(data);
            case "ORDERING" -> validateOrdering(data);
            default -> throw invalid("Unknown exercise type: " + type);
        }
    }

    private static void validateMultipleChoice(Map<String, Object> data)
    {
        List<String> options = texts(data, "options", 2);

        if (!(data.get("correctIndex") instanceof Integer index) || index < 0 || index >= options.size())
        {
            throw invalid("'correctIndex' must be a whole number pointing to one of the options");
        }
    }

    private static void validateMatching(Map<String, Object> data)
    {
        if (!(data.get("pairs") instanceof List<?> pairs) || pairs.size() < 2 || pairs.size() > MAX_ITEMS)
        {
            throw invalid("'pairs' must be a list with 2 to " + MAX_ITEMS + " items");
        }

        for (Object pair : pairs)
        {
            if (!(pair instanceof Map<?, ?> entry))
            {
                throw invalid("Every pair must have a 'left' and a 'right' text");
            }
            text(entry.get("left"), "pairs.left");
            text(entry.get("right"), "pairs.right");
        }
    }

    private static void validateOrdering(Map<String, Object> data)
    {
        List<String> words = texts(data, "words", 2);
        List<String> correctOrder = texts(data, "correctOrder", 2);

        List<String> sortedWords = new ArrayList<>(words);
        List<String> sortedOrder = new ArrayList<>(correctOrder);
        Collections.sort(sortedWords);
        Collections.sort(sortedOrder);
        if (!sortedWords.equals(sortedOrder))
        {
            throw invalid("'correctOrder' must contain exactly the same words as 'words'");
        }

        Object translation = data.get("translation");
        if (translation != null && (!(translation instanceof String hint) || hint.length() > MAX_TEXT_LENGTH))
        {
            throw invalid("'translation' must be a text of at most " + MAX_TEXT_LENGTH + " characters");
        }
    }

    private static List<String> texts(Map<String, Object> data, String key, int min)
    {
        if (!(data.get(key) instanceof List<?> items) || items.size() < min || items.size() > MAX_ITEMS)
        {
            throw invalid("'" + key + "' must be a list with " + min + " to " + MAX_ITEMS + " items");
        }

        List<String> result = new ArrayList<>(items.size());
        for (Object item : items)
        {
            result.add(text(item, key));
        }
        return result;
    }

    private static String text(Object value, String field)
    {
        if (!(value instanceof String text) || text.isBlank() || text.length() > MAX_TEXT_LENGTH)
        {
            throw invalid("Every value of '" + field + "' must be a non-blank text of at most "
                    + MAX_TEXT_LENGTH + " characters");
        }
        return text;
    }

    private static InvalidRequestException invalid(String message)
    {
        return new InvalidRequestException(message);
    }
}