package com.chineselearning.progressservice.evaluation;

import com.chineselearning.progressservice.exception.ExerciseConfigurationException;
import com.chineselearning.progressservice.exception.InvalidAnswerException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvaluationServiceTest {

    private final EvaluationService service = new EvaluationService();

    @Test
    void translationIgnoresChinesePunctuationAndSpaces() {
        Map<String, Object> content = Map.of("acceptedAnswers", List.of("我喜欢喝茶"));
        assertTrue(service.evaluate("TRANSLATION", content, Map.of("translation", "我喜欢喝茶。")).isCorrect());
        assertTrue(service.evaluate("TRANSLATION", content, Map.of("translation", "我 喜欢 喝茶！")).isCorrect());
    }

    @Test
    void chineseTranslationGetsPartialCredit() {
        Map<String, Object> content = Map.of("acceptedAnswers", List.of("我喜欢喝茶"));
        EvaluationResult result = service.evaluate("TRANSLATION", content, Map.of("translation", "我喜欢喝咖啡"));
        assertEquals(0, result.score().compareTo(new BigDecimal("50")));
        assertFalse(result.isCorrect());
    }

    @Test
    void malformedAnswerIsAClientError() {
        Map<String, Object> content = Map.of("correctAnswers", List.of("你好"));
        assertThrows(InvalidAnswerException.class,
                () -> service.evaluate("FILL_BLANK", content, Map.of("answers", List.of(1, 2))));
    }

    @Test
    void malformedExerciseIsAServerError() {
        Map<String, Object> content = Map.of("options", List.of("a"), "correctIndex", 5);
        assertThrows(ExerciseConfigurationException.class,
                () -> service.evaluate("MULTIPLE_CHOICE", content, Map.of("selectedIndex", 0)));
    }

    @Test
    void passThresholdIsKeptAt70() {
        Map<String, Object> content = Map.of("correctOrder", List.of("a", "b", "c", "d"));
        EvaluationResult result = service.evaluate("ORDERING", content, Map.of("order", List.of("a", "b", "c", "x")));
        assertTrue(result.isCorrect());
    }
}