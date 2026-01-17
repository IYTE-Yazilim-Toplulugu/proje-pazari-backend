package com.iyte_yazilim.proje_pazari.domain.models.results;

/**
 * Result returned after successful user authentication.
 *
 * <p>Contains user information, access token, and refresh token for subsequent requests.
 *
 * @param userId the authenticated user's unique identifier
 * @param email the authenticated user's email address
 * @param firstName the user's first name
 * @param lastName the user's last name
 * @param role the user's role
 * @param accessToken the JWT authentication token for API access (short-lived)
 * @param refreshToken the refresh token for obtaining new access tokens (long-lived)
 * @param expiresIn time in milliseconds until the access token expires
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2024-01-01
 * @see com.iyte_yazilim.proje_pazari.application.commands.loginUser.LoginUserCommand
 */
public record LoginUserResult(
        String userId,
        String email,
        String firstName,
        String lastName,
        String role,
        String accessToken,
        String refreshToken,
        long expiresIn) {}
