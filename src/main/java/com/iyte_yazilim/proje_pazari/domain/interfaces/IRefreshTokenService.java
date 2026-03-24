package com.iyte_yazilim.proje_pazari.domain.interfaces;

import java.util.Optional;

/**
 * Domain interface for refresh token operations.
 *
 * <p>Abstracts refresh token management so application-layer handlers can depend on this interface
 * rather than importing concrete infrastructure classes.
 */
public interface IRefreshTokenService {

    String createRefreshToken(String userId);

    Optional<String> validateRefreshToken(String token);

    void revokeRefreshToken(String token);

    /**
     * Validates a refresh token and revokes it in a single transaction.
     *
     * @param token the refresh token to validate and revoke
     * @return Optional containing the owner userId if the token was valid, empty otherwise
     */
    Optional<String> validateAndRevoke(String token);
}
