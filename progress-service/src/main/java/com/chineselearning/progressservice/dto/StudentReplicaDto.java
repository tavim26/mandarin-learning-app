package com.chineselearning.progressservice.dto;

import com.chineselearning.progressservice.entity.StudentReplica;

public record StudentReplicaDto(Long studentId, Integer xpTotal, Integer level) {

    public static StudentReplicaDto from(StudentReplica r) {
        return new StudentReplicaDto(r.getStudentId(), r.getXpTotal(), r.getLevel());
    }
}