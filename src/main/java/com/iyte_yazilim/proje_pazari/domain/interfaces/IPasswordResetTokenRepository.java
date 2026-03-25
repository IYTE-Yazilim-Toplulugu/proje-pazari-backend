package com.iyte_yazilim.proje_pazari.domain.interfaces;

import com.iyte_yazilim.proje_pazari.domain.entities.PasswordResetToken;
import java.util.Optional;

/** Domain repository interface for password reset token operations. */
public interface IPasswordResetTokenRepository {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUserId(String userId);

    PasswordResetToken save(PasswordResetToken token);
}
