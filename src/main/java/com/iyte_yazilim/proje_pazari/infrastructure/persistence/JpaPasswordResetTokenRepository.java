package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import com.iyte_yazilim.proje_pazari.domain.entities.PasswordResetToken;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IPasswordResetTokenRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.PasswordResetTokenMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adapter that bridges the Spring Data {@link PasswordResetTokenRepository} to the domain
 * interface.
 */
@Component
@RequiredArgsConstructor
public class JpaPasswordResetTokenRepository implements IPasswordResetTokenRepository {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetTokenMapper mapper;

    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        return passwordResetTokenRepository.findByToken(token).map(mapper::entityToDomain);
    }

    @Override
    public void deleteByUserId(String userId) {
        passwordResetTokenRepository.deleteByUserId(userId);
    }

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        return mapper.entityToDomain(
                passwordResetTokenRepository.save(mapper.domainToEntity(token)));
    }
}
