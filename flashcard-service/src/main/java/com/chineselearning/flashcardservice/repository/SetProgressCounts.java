package com.chineselearning.flashcardservice.repository;

public interface SetProgressCounts {

    Long getSetId();

    long getSeen();

    long getDue();
}