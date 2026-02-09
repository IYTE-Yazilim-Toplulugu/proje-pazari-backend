package com.iyte_yazilim.proje_pazari.domain.models.results;

/**
 * Result returned after successful user registration.
 *
 * <p>Contains the essential user information after a new account is created.
 *
 * @param userId the unique ULID identifier assigned to the new user
 * @param email the registered email address
 * @param firstName the user's first name
 * @param lastName the user's last name
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2024-01-01
 * @see com.iyte_yazilim.proje_pazari.application.commands.registerUser.RegisterUserCommand
 */
public record RegisterUserResult(String userId, String email, String firstName, String lastName) {}
