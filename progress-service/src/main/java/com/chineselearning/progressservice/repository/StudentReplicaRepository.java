package com.chineselearning.progressservice.repository;

import com.chineselearning.progressservice.entity.StudentReplica;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentReplicaRepository extends JpaRepository<StudentReplica, Long>
{

    @Modifying
    @Query(value = """
            INSERT INTO students_replica (student_id, xp_total, level)
            VALUES (:studentId, 0, 1)
            ON CONFLICT (student_id) DO NOTHING
            """, nativeQuery = true)
    void insertIfAbsent(@Param("studentId") Long studentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from StudentReplica r where r.studentId = :studentId")
    Optional<StudentReplica> findByIdForUpdate(@Param("studentId") Long studentId);

    List<StudentReplica> findTop10ByOrderByXpTotalDescStudentIdAsc();
}