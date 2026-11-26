package com.chineselearning.userservice.repository;

import com.chineselearning.userservice.entity.CredentialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CredentialRepository extends JpaRepository<CredentialEntity, Long>
{
    Optional<CredentialEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}