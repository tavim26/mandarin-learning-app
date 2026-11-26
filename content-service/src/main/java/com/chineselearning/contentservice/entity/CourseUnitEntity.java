package com.chineselearning.contentservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "course_units")
public class CourseUnitEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "hsk_level")
    private Integer hskLevel;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "created_by_teacher_id")
    private Long createdByTeacherId;

    // Only here so that deleting a unit also deletes its lessons, exercises and materials.
    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LessonEntity> lessons = new ArrayList<>();

    protected CourseUnitEntity() {}

    public CourseUnitEntity(String title, String description, Integer hskLevel, Integer orderIndex, Long createdByTeacherId)
    {
        this.title = title;
        this.description = description;
        this.hskLevel = hskLevel;
        this.orderIndex = orderIndex;
        this.createdByTeacherId = createdByTeacherId;
    }

    public void update(String title, String description, Integer hskLevel, Integer orderIndex)
    {
        this.title = title;
        this.description = description;
        this.hskLevel = hskLevel;
        this.orderIndex = orderIndex;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Integer getHskLevel() { return hskLevel; }
    public Integer getOrderIndex() { return orderIndex; }
    public Long getCreatedByTeacherId() { return createdByTeacherId; }
}