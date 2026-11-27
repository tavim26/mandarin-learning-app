package com.chineselearning.flashcardservice.scheduling;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;


public final class Sm2Scheduler {

    public static final BigDecimal INITIAL_EASINESS_FACTOR = new BigDecimal("2.5");

    private static final BigDecimal MIN_EASINESS_FACTOR = new BigDecimal("1.3");
    private static final BigDecimal FIVE = BigDecimal.valueOf(5);
    private static final BigDecimal BASE = new BigDecimal("0.10");
    private static final BigDecimal LINEAR = new BigDecimal("0.08");
    private static final BigDecimal QUADRATIC = new BigDecimal("0.02");
    private static final int FIRST_INTERVAL_DAYS = 1;
    private static final int SECOND_INTERVAL_DAYS = 6;
    private static final int PASSING_QUALITY = 3;

    private Sm2Scheduler() {
    }

    public record Result(BigDecimal easinessFactor, int intervalDays, int repetitionCount, LocalDateTime nextReviewAt) {
    }

    public static Result schedule(BigDecimal easinessFactor, int intervalDays, int repetitionCount, int quality, LocalDate today) {
        if (quality < 0 || quality > 5) {
            throw new IllegalArgumentException("quality must be between 0 and 5");
        }
        BigDecimal newEf = nextEasinessFactor(easinessFactor, quality);

        int newRepetitions;
        int newInterval;
        if (quality < PASSING_QUALITY) {
            newRepetitions = 0;
            newInterval = FIRST_INTERVAL_DAYS;
        } else {
            newRepetitions = repetitionCount + 1;
            newInterval = nextInterval(intervalDays, newRepetitions, newEf);
        }
        return new Result(newEf, newInterval, newRepetitions, today.plusDays(newInterval).atStartOfDay());
    }

    private static BigDecimal nextEasinessFactor(BigDecimal ef, int quality) {
        BigDecimal miss = FIVE.subtract(BigDecimal.valueOf(quality));
        BigDecimal delta = BASE.subtract(miss.multiply(LINEAR.add(miss.multiply(QUADRATIC))));
        BigDecimal result = ef.add(delta).setScale(2, RoundingMode.HALF_UP);
        return result.compareTo(MIN_EASINESS_FACTOR) < 0 ? MIN_EASINESS_FACTOR : result;
    }

    private static int nextInterval(int currentInterval, int repetitions, BigDecimal ef) {
        if (repetitions == 1) {
            return FIRST_INTERVAL_DAYS;
        }
        if (repetitions == 2) {
            return SECOND_INTERVAL_DAYS;
        }
        return BigDecimal.valueOf(currentInterval).multiply(ef).setScale(0, RoundingMode.HALF_UP).intValueExact();
    }
}