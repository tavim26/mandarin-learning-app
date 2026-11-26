package com.chineselearning.userservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserEntity
{
    @Id
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @OneToOne(fetch = FetchType.EAGER)
    @MapsId
    @JoinColumn(name = "id")
    private CredentialEntity credential;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private StudentEntity student;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private TeacherEntity teacher;

    protected UserEntity() {}

    public UserEntity(String fullName)
    {
        this.fullName = fullName;
    }

    public void attachStudent(StudentEntity student)
    {
        this.student = student;
        student.setUser(this);
    }

    public void attachTeacher(TeacherEntity teacher)
    {
        this.teacher = teacher;
        teacher.setUser(this);
    }

    public Long getId() { return id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public CredentialEntity getCredential() { return credential; }
    void setCredential(CredentialEntity credential) { this.credential = credential; }

    public StudentEntity getStudent() { return student; }

    public TeacherEntity getTeacher() { return teacher; }
}