package com.chineselearning.progressservice.evaluation;

import java.math.BigDecimal;

public record EvaluationResult(BigDecimal score, String feedback) {

    public static final BigDecimal PASS_THRESHOLD = new BigDecimal("70");

    public boolean isCorrect() {
        return score.compareTo(PASS_THRESHOLD) >= 0;
    }
}