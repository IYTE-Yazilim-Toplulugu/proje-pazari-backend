package com.iyte_yazilim.proje_pazari.domain.interfaces;

import java.util.Date;

/**
 * Domain interface for JWT token operations.
 *
 * <p>Abstracts token generation and inspection so application-layer handlers can depend on this
 * interface rather than importing from the presentation layer.
 */
public interface ITokenService {

    String generateToken(String userId, String email, String role);

    Date extractExpiration(String token);
}
