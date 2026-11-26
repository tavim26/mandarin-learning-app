package com.chineselearning.contentservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "exercises")
public class ExerciseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private LessonEntity lesson;

    @Column(nullable = false)
    private String type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String prompt;

    private Integer difficulty;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content_data", columnDefinition = "jsonb")
    private Map<String, Object> contentData;

    protected ExerciseEntity() {}

    public ExerciseEntity(LessonEntity lesson, String type, String prompt, Integer difficulty, Map<String, Object> contentData)
    {
        this.lesson = lesson;
        this.type = type;
        this.prompt = prompt;
        this.difficulty = difficulty;
        this.contentData = contentData;
    }

    public void update(String type, String prompt, Integer difficulty, Map<String, Object> contentData)
    {
        this.type = type;
        this.prompt = prompt;
        this.difficulty = difficulty;
        this.contentData = contentData;
    }

    public Long getId() { return id; }
    public LessonEntity getLesson() { return lesson; }
    public String getType() { return type; }
    public String getPrompt() { return prompt; }
    public Integer getDifficulty() { return difficulty; }
    public Map<String, Object> getContentData() { return contentData; }
}