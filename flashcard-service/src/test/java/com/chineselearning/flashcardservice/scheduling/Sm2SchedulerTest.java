package com.chineselearning.flashcardservice.scheduling;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Sm2SchedulerTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 27);

    @Test
    void goodAnswersGrowIntervalsTheSm2Way() {
        BigDecimal ef = Sm2Scheduler.INITIAL_EASINESS_FACTOR;
        int interval = 0;
        int repetitions = 0;
        for (int expected : new int[]{1, 6, 15, 38, 95}) {
            Sm2Scheduler.Result r = Sm2Scheduler.schedule(ef, interval, repetitions, 4, TODAY);
            assertEquals(expected, r.intervalDays());
            assertEquals(0, new BigDecimal("2.5").compareTo(r.easinessFactor()));
            ef = r.easinessFactor();
            interval = r.intervalDays();
            repetitions = r.repetitionCount();
        }
    }

    @Test
    void lapseResetsRepetitionsAndInterval() {
        Sm2Scheduler.Result r = Sm2Scheduler.schedule(new BigDecimal("2.5"), 38, 4, 1, TODAY);
        assertEquals(0, r.repetitionCount());
        assertEquals(1, r.intervalDays());
    }

    @Test
    void easinessFactorNeverDropsBelowFloor() {
        BigDecimal ef = new BigDecimal("1.30");
        for (int i = 0; i < 20; i++) {
            ef = Sm2Scheduler.schedule(ef, 1, 0, 0, TODAY).easinessFactor();
        }
        assertEquals(0, new BigDecimal("1.3").compareTo(ef));
    }

    @Test
    void cardsAreDueAtTheStartOfTheDay() {
        Sm2Scheduler.Result r = Sm2Scheduler.schedule(new BigDecimal("2.5"), 1, 1, 5, TODAY);
        assertEquals(TODAY.plusDays(6).atStartOfDay(), r.nextReviewAt());
    }

    @Test
    void roundsHalfUpWithoutFloatingPointArtifacts() {
        Sm2Scheduler.Result r = Sm2Scheduler.schedule(new BigDecimal("1.91"), 3650, 2, 5, TODAY);
        assertEquals(7337, r.intervalDays());
    }

    @Test
    void invalidQualityIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> Sm2Scheduler.schedule(new BigDecimal("2.5"), 1, 1, 6, TODAY));
    }
}