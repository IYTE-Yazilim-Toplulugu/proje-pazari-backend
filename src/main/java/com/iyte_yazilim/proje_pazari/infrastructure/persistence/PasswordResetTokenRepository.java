package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.PasswordResetTokenEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/** Repository for password reset token operations. */
public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetTokenEntity, String> {

    Optional<PasswordResetTokenEntity> findByToken(String token);

    @Transactional
    @Modifying
    @Query("DELETE FROM PasswordResetTokenEntity p WHERE p.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);
}
