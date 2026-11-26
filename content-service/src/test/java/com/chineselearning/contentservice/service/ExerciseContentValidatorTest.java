package com.chineselearning.contentservice.service;

import com.chineselearning.contentservice.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExerciseContentValidatorTest
{
    private static void assertInvalid(String type, Map<String, Object> data)
    {
        assertThrows(InvalidRequestException.class, () -> ExerciseContentValidator.validate(type, data));
    }

    @Test
    void acceptsAWellFormedMultipleChoice()
    {
        assertDoesNotThrow(() -> ExerciseContentValidator.validate("MULTIPLE_CHOICE",
                Map.of("options", List.of("a", "b", "c"), "correctIndex", 2)));
    }

    @Test
    void rejectsACorrectIndexOutsideTheOptions()
    {
        assertInvalid("MULTIPLE_CHOICE", Map.of("options", List.of("a", "b"), "correctIndex", 2));
    }

    @Test
    void rejectsACorrectIndexThatIsNotAnInteger()
    {
        // The evaluator casts to Integer, so 1.0 would never match.
        assertInvalid("MULTIPLE_CHOICE", Map.of("options", List.of("a", "b"), "correctIndex", 1.0));
    }

    @Test
    void rejectsContentWithTheWrongKeyForItsType()
    {
        assertInvalid("TRANSLATION", Map.of("answers", List.of("hello")));
    }

    @Test
    void rejectsAMatchingExerciseWithASinglePair()
    {
        assertInvalid("MATCHING", Map.of("pairs", List.of(Map.of("left", "你", "right", "you"))));
    }

    @Test
    void acceptsAnOrderingExerciseWhoseWordsMatchTheCorrectOrder()
    {
        assertDoesNotThrow(() -> ExerciseContentValidator.validate("ORDERING",
                Map.of("words", List.of("吃", "我", "饭"), "correctOrder", List.of("我", "吃", "饭"), "translation", "")));
    }

    @Test
    void rejectsAnOrderingExerciseThatCanNeverBeSolved()
    {
        assertInvalid("ORDERING",
                Map.of("words", List.of("吃", "我", "饭"), "correctOrder", List.of("我", "吃", "面")));
    }

    @Test
    void rejectsUnknownTypesAndMissingContent()
    {
        assertInvalid("DRAG_DROP", Map.of());
        assertInvalid("FILL_BLANK", null);
    }
}