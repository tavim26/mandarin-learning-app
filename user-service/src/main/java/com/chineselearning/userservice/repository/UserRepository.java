package com.chineselearning.userservice.repository;

import com.chineselearning.userservice.entity.Role;
import com.chineselearning.userservice.entity.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<UserEntity, Long>
{
    @EntityGraph(attributePaths = {"credential", "student", "teacher"})
    List<UserEntity> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = {"credential", "student", "teacher"})
    List<UserEntity> findByFullNameContainingIgnoreCaseOrderByIdAsc(String fragment);

    @EntityGraph(attributePaths = {"credential", "student", "teacher"})
    List<UserEntity> findByCredential_RoleOrderByIdAsc(Role role);
}