package com.iyte_yazilim.proje_pazari.domain.interfaces;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.PasswordResetTokenEntity;
import java.util.Optional;

/** Domain repository interface for password reset token operations. */
public interface IPasswordResetTokenRepository {

    Optional<PasswordResetTokenEntity> findByToken(String token);

    void deleteByUserId(String userId);

    PasswordResetTokenEntity save(PasswordResetTokenEntity entity);
}
