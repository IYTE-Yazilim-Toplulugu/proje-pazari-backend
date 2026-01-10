package com.iyte_yazilim.proje_pazari.domain.models.results;

/**
 * Result returned after successful user authentication.
 *
 * <p>Contains user information and the JWT token for subsequent requests.
 *
 * @param userId the authenticated user's unique identifier
 * @param email the authenticated user's email address
 * @param firstName the user's first name
 * @param lastName the user's last name
 * @param token the JWT authentication token for API access
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
        String token) {}
