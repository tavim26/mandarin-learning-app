package com.chineselearning.progressservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "students_replica",
        indexes = @Index(name = "idx_students_xp_leaderboard", columnList = "xp_total DESC"))
public class StudentReplica {

    private static final int XP_PER_LEVEL = 100;

    @Id
    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "xp_total", nullable = false)
    private Integer xpTotal;

    @Column(name = "level", nullable = false)
    private Integer level;

    protected StudentReplica() {
    }

    public StudentReplica(Long studentId) {
        this.studentId = studentId;
        this.xpTotal = 0;
        this.level = 1;
    }

    public void addXp(int xpToAdd) {
        if (xpToAdd < 0) {
            throw new IllegalArgumentException("xpToAdd must not be negative");
        }
        this.xpTotal += xpToAdd;
        this.level = (this.xpTotal / XP_PER_LEVEL) + 1;
    }

    public Long getStudentId() {
        return studentId;
    }

    public Integer getXpTotal() {
        return xpTotal;
    }

    public Integer getLevel() {
        return level;
    }
}