package com.chineselearning.userservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "teachers")
public class TeacherEntity
{
    @Id
    private Long userId;

    @Column(name = "title", length = 100)
    private String title;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private UserEntity user;

    public TeacherEntity()
    {
        // The current frontend expects a string, never null.
        this.title = "";
    }

    public Long getUserId() { return userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public UserEntity getUser() { return user; }
    void setUser(UserEntity user) { this.user = user; }
}