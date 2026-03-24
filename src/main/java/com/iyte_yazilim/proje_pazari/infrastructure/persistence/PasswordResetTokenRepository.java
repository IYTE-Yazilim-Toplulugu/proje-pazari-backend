package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IPasswordResetTokenRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.PasswordResetTokenEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository for password reset token operations. */
public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetTokenEntity, String>, IPasswordResetTokenRepository {

    Optional<PasswordResetTokenEntity> findByToken(String token);

    @Modifying
    @Query("DELETE FROM PasswordResetTokenEntity p WHERE p.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);
}
