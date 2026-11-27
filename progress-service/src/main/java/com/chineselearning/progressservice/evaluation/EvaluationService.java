package com.chineselearning.progressservice.evaluation;

import com.chineselearning.progressservice.exception.ExerciseConfigurationException;
import com.chineselearning.progressservice.exception.InvalidAnswerException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class EvaluationService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal PARTIAL_TRANSLATION_THRESHOLD = new BigDecimal("40");
    private static final BigDecimal PARTIAL_TRANSLATION_SCORE = new BigDecimal("50");
    private static final BigDecimal ALMOST_ORDERED_THRESHOLD = new BigDecimal("70");

    public EvaluationResult evaluate(String exerciseType,
                                     Map<String, Object> contentData,
                                     Map<String, Object> submittedAnswer) {
        ExerciseType type = ExerciseType.parse(exerciseType);

        if (contentData == null)
        {
            throw new ExerciseConfigurationException("Exercise has no content data");
        }

        if (submittedAnswer == null)
        {
            throw new InvalidAnswerException("submittedAnswer is mandatory");
        }
        TypedReader content = new TypedReader(contentData, ExerciseConfigurationException::new);
        TypedReader answer = new TypedReader(submittedAnswer, InvalidAnswerException::new);

        return switch (type) {
            case MULTIPLE_CHOICE -> multipleChoice(content, answer);
            case TRANSLATION -> translation(content, answer);
            case FILL_BLANK -> fillBlank(content, answer);
            case MATCHING -> matching(content, answer);
            case ORDERING -> ordering(content, answer);
        };
    }

    private EvaluationResult multipleChoice(TypedReader content, TypedReader answer)
    {
        List<String> options = content.texts("options");
        Integer correctIndex = content.integer("correctIndex");

        if (options == null || correctIndex == null || correctIndex < 0 || correctIndex >= options.size())
        {
            throw new ExerciseConfigurationException("Invalid multiple choice configuration");
        }

        Integer selectedIndex = answer.integer("selectedIndex");

        if (selectedIndex == null)
        {
            return new EvaluationResult(BigDecimal.ZERO, "No option selected.");
        }

        if (correctIndex.equals(selectedIndex))
        {
            return new EvaluationResult(HUNDRED, "Correct!");
        }

        return new EvaluationResult(BigDecimal.ZERO,
                "Incorrect. The correct answer was: " + options.get(correctIndex));
    }

    private EvaluationResult translation(TypedReader content, TypedReader answer)
    {
        List<String> accepted = content.texts("acceptedAnswers");

        if (accepted == null || accepted.isEmpty())
        {
            throw new ExerciseConfigurationException("Invalid translation configuration");
        }

        String translation = answer.text("translation");

        List<String> userTokens = translation == null ? List.of() : TextNormalizer.tokens(translation);

        if (userTokens.isEmpty())
        {
            return new EvaluationResult(BigDecimal.ZERO, "No translation provided.");
        }

        for (String candidate : accepted)
        {
            if (TextNormalizer.tokens(candidate).equals(userTokens))
            {
                return new EvaluationResult(HUNDRED, "Correct translation!");
            }
        }

        String reference = accepted.get(0);

        BigDecimal overlap = overlapScore(userTokens, TextNormalizer.tokens(reference));

        if (overlap.compareTo(PARTIAL_TRANSLATION_THRESHOLD) >= 0)
        {
            return new EvaluationResult(PARTIAL_TRANSLATION_SCORE,
                    "Partially correct (" + overlap.toPlainString() + "% words matched). Expected: " + reference);
        }
        return new EvaluationResult(BigDecimal.ZERO, "Incorrect. Correct translation: " + reference);
    }


    private EvaluationResult fillBlank(TypedReader content, TypedReader answer)
    {
        List<String> correctAnswers = content.texts("correctAnswers");

        if (correctAnswers == null || correctAnswers.isEmpty())
        {
            throw new ExerciseConfigurationException("Invalid fill-in-the-blank configuration");
        }
        List<String> userAnswers = answer.texts("answers");

        if (userAnswers == null)
        {
            return new EvaluationResult(BigDecimal.ZERO, "No answers provided.");
        }

        if (userAnswers.size() != correctAnswers.size())
        {
            return new EvaluationResult(BigDecimal.ZERO,
                    "Incorrect number of answers. Expected: " + correctAnswers.size());
        }

        int correct = 0;
        for (int i = 0; i < correctAnswers.size(); i++)
        {
            if (TextNormalizer.sameText(correctAnswers.get(i), userAnswers.get(i)))
            {
                correct++;
            }
        }
        String feedback = correct == correctAnswers.size()
                ? "All answers are correct!"
                : String.format("You filled %d out of %d blanks correctly.", correct, correctAnswers.size());
        return new EvaluationResult(percent(correct, correctAnswers.size()), feedback);
    }

    private EvaluationResult matching(TypedReader content, TypedReader answer)
    {
        List<Map<String, String>> pairs = content.textMaps("pairs");

        if (pairs == null || pairs.isEmpty())
        {
            throw new ExerciseConfigurationException("Invalid matching configuration");
        }

        Map<String, String> userMatches = answer.textMap("matches");

        if (userMatches == null || userMatches.isEmpty())
        {
            return new EvaluationResult(BigDecimal.ZERO, "No matches provided.");
        }
        int correct = 0;

        for (Map<String, String> pair : pairs)
        {
            String left = pair.get("left");
            String right = pair.get("right");

            if (left == null || right == null)
            {
                throw new ExerciseConfigurationException("Matching pair without left/right");
            }

            if (right.equals(userMatches.get(left)))
            {
                correct++;
            }
        }
        String feedback = correct == pairs.size()
                ? "All pairs matched correctly!"
                : String.format("You matched %d out of %d pairs correctly.", correct, pairs.size());
        return new EvaluationResult(percent(correct, pairs.size()), feedback);
    }


    private EvaluationResult ordering(TypedReader content, TypedReader answer)
    {
        List<String> correctOrder = content.texts("correctOrder");
        if (correctOrder == null || correctOrder.isEmpty())
        {
            throw new ExerciseConfigurationException("Invalid ordering configuration");
        }

        List<String> submitted = answer.texts("order");

        if (submitted == null || submitted.isEmpty())
        {
            return new EvaluationResult(BigDecimal.ZERO, "No order provided.");
        }

        if (submitted.size() != correctOrder.size())
        {
            return new EvaluationResult(BigDecimal.ZERO,
                    "Incorrect number of words. Expected: " + correctOrder.size());
        }

        if (submitted.equals(correctOrder))
        {
            return new EvaluationResult(HUNDRED, "Correct! The sentence order is right.");
        }

        int inPlace = 0;
        for (int i = 0; i < correctOrder.size(); i++)
        {
            if (correctOrder.get(i).equals(submitted.get(i)))
            {
                inPlace++;
            }
        }

        BigDecimal score = percent(inPlace, correctOrder.size());
        if (score.compareTo(ALMOST_ORDERED_THRESHOLD) >= 0)
        {
            return new EvaluationResult(score, String.format("Almost correct! %d out of %d words in the right position.", inPlace, correctOrder.size()));
        }
        return new EvaluationResult(score, String.format("Incorrect. You placed %d out of %d words correctly. Correct order: %s",
                inPlace, correctOrder.size(), String.join(" ", correctOrder)));
    }

    private static BigDecimal percent(int part, int total)
    {
        return BigDecimal.valueOf((part * 100.0) / total).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal overlapScore(List<String> userTokens, List<String> referenceTokens)
    {
        Set<String> reference = new HashSet<>(referenceTokens);

        if (reference.isEmpty())
        {
            return BigDecimal.ZERO;
        }

        Set<String> user = new HashSet<>(userTokens);
        long common = user.stream().filter(reference::contains).count();
        return BigDecimal.valueOf((common * 100.0) / reference.size()).setScale(2, RoundingMode.HALF_UP);
    }
}