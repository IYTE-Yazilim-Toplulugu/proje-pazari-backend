package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.EmailVerificationEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for email verification operations. */
public interface EmailVerificationRepository
        extends JpaRepository<EmailVerificationEntity, String> {

    Optional<EmailVerificationEntity> findByToken(String token);

    Optional<EmailVerificationEntity> findByEmailAndVerifiedAtIsNull(String email);

    Optional<EmailVerificationEntity> findTopByUserIdOrderByCreatedAtDesc(String userId);

    boolean existsByEmailAndVerifiedAtIsNotNull(String email);
}
