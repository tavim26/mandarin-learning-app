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

@Entity
@Table(name = "lesson_materials")
public class LessonMaterialEntity
{
    public static final String TYPE_LINK = "link";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private LessonEntity lesson;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, length = 1000)
    private String url;

    protected LessonMaterialEntity() {}

    public LessonMaterialEntity(LessonEntity lesson, String title, String type, String url)
    {
        this.lesson = lesson;
        this.title = title;
        this.type = type;
        this.url = url;
    }

    public boolean isLink()
    {
        return TYPE_LINK.equalsIgnoreCase(type);
    }

    public Long getId() { return id; }
    public LessonEntity getLesson() { return lesson; }
    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getUrl() { return url; }
}