package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;

import java.util.Optional;

/**
 * UserRepository - JPA Repository for user persistence layer
 */
public interface UserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByVerificationToken(String token);
    boolean existsByEmail(String email);
}
