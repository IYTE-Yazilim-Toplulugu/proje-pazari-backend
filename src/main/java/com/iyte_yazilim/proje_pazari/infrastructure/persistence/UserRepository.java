package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;

import java.util.Optional;

/**
 * UserRepository - JPA Repository for user persistence layer
 */
public interface UserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT COUNT(p) FROM ProjectEntity p WHERE p.owner.id = :userId")
    int countProjectsByUserId(@Param("userId") String userId);

    @Query("SELECT COUNT(a) FROM ProjectApplicationEntity a WHERE a.user.id = :userId")
    int countApplicationsByUserId(@Param("userId") String userId);
}
