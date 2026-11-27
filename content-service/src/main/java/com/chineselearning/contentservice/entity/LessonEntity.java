package com.chineselearning.contentservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lessons")
public class LessonEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unit_id", nullable = false)
    private CourseUnitEntity unit;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "xp_reward", nullable = false)
    private Integer xpReward;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExerciseEntity> exercises = new ArrayList<>();

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LessonMaterialEntity> materials = new ArrayList<>();

    protected LessonEntity() {}

    public LessonEntity(CourseUnitEntity unit, String title, String description, Integer xpReward, Integer orderIndex)
    {
        this.unit = unit;
        this.title = title;
        this.description = description;
        this.xpReward = xpReward;
        this.orderIndex = orderIndex;
    }

    public void update(String title, String description, Integer xpReward, Integer orderIndex)
    {
        this.title = title;
        this.description = description;
        this.xpReward = xpReward;
        this.orderIndex = orderIndex;
    }

    public void moveTo(CourseUnitEntity unit)
    {
        this.unit = unit;
    }

    public Long getId() { return id; }
    public CourseUnitEntity getUnit() { return unit; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Integer getXpReward() { return xpReward; }
    public Integer getOrderIndex() { return orderIndex; }
}