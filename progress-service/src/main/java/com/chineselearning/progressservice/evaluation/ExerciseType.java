package com.chineselearning.progressservice.evaluation;

import com.chineselearning.progressservice.exception.ExerciseConfigurationException;

enum ExerciseType {
    MULTIPLE_CHOICE,
    TRANSLATION,
    FILL_BLANK,
    MATCHING,
    ORDERING;

    static ExerciseType parse(String raw) {
        if (raw == null) {
            throw new ExerciseConfigurationException("Exercise has no type");
        }
        try {
            return valueOf(raw);
        } catch (IllegalArgumentException e) {
            throw new ExerciseConfigurationException("Unknown exercise type: " + raw);
        }
    }
}