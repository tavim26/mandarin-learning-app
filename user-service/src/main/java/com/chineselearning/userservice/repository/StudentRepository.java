package com.chineselearning.userservice.repository;

import com.chineselearning.userservice.entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<StudentEntity, Long>
{
    List<StudentEntity> findByNicknameContainingIgnoreCase(String fragment);

    boolean existsByNicknameAndUserIdNot(String nickname, Long userId);
}