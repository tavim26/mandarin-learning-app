package com.chineselearning.contentservice.domain.dto;

import jakarta.validation.constraints.*;

public class CourseUnitDto {

    private Long id;

    @NotBlank(message = "Unit title is mandatory")
    @Size(max = 255, message = "Unit title must not exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    // Optional: a unit may have no HSK level
    @Min(value = 1, message = "HSK level must be between 1 and 6")
    @Max(value = 6, message = "HSK level must be between 1 and 6")
    private Integer hskLevel;

    @NotNull(message = "Order index is mandatory")
    @Min(value = 0, message = "Order index cannot be negative")
    private Integer orderIndex;

    private Long createdByTeacherId;

    public CourseUnitDto() {}

    public CourseUnitDto(Long id, String title, String description, Integer hskLevel, Integer orderIndex) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.hskLevel = hskLevel;
        this.orderIndex = orderIndex;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getHskLevel() { return hskLevel; }
    public void setHskLevel(Integer hskLevel) { this.hskLevel = hskLevel; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public Long getCreatedByTeacherId() { return createdByTeacherId; }
    public void setCreatedByTeacherId(Long createdByTeacherId) { this.createdByTeacherId = createdByTeacherId; }
}