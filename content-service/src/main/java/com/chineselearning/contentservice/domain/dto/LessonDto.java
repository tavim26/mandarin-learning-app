package com.chineselearning.contentservice.domain.dto;

import jakarta.validation.constraints.*;

import java.util.List;

public class LessonDto {

    private Long id;

    @NotNull(message = "Unit id is mandatory")
    private Long unitId;

    @NotBlank(message = "Lesson title is mandatory")
    @Size(max = 255, message = "Lesson title must not exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotNull(message = "XP reward is mandatory")
    @Min(value = 0, message = "XP reward cannot be negative")
    @Max(value = 1000, message = "XP reward must not exceed 1000")
    private Integer xpReward;

    @NotNull(message = "Order index is mandatory")
    @Min(value = 0, message = "Order index cannot be negative")
    private Integer orderIndex;
    private List<ExerciseDto> exercises;

    public LessonDto() {
    }

    public LessonDto(Long id, Long unitId, String title, String description, Integer xpReward, Integer orderIndex) {
        this.id = id;
        this.unitId = unitId;
        this.title = title;
        this.description = description;
        this.xpReward = xpReward;
        this.orderIndex = orderIndex;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getXpReward() {
        return xpReward;
    }

    public void setXpReward(Integer xpReward) {
        this.xpReward = xpReward;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public List<ExerciseDto> getExercises() {
        return exercises;
    }

    public void setExercises(List<ExerciseDto> exercises) {
        this.exercises = exercises;
    }
}