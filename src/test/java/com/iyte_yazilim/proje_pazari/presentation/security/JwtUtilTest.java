package com.iyte_yazilim.proje_pazari.presentation.security;

import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.ExpiredJwtException;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();

        // Set the secret using reflection since @Value is not available in unit tests
        Field secretField = JwtUtil.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(
                jwtUtil,
                "test-secret-key-for-testing-purposes-must-be-at-least-256-bits-long-for-hs256-algorithm");

        // Set expiration to 1 hour (3600000 ms)
        Field expirationField = JwtUtil.class.getDeclaredField("expiration");
        expirationField.setAccessible(true);
        expirationField.set(jwtUtil, 3600000L);
    }

    @Test
    @DisplayName("Should generate a valid JWT token")
    void shouldGenerateToken() {
        // When
        String token = jwtUtil.generateToken("test@std.iyte.edu.tr");

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        // JWT tokens have 3 parts separated by dots
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    @DisplayName("Should extract username from token")
    void shouldExtractUsername() {
        // Given
        String email = "test@std.iyte.edu.tr";
        String token = jwtUtil.generateToken(email);

        // When
        String extractedUsername = jwtUtil.extractUsername(token);

        // Then
        assertEquals(email, extractedUsername);
    }

    @Test
    @DisplayName("Should extract expiration date from token")
    void shouldExtractExpiration() {
        // Given
        String token = jwtUtil.generateToken("test@std.iyte.edu.tr");

        // When
        var expiration = jwtUtil.extractExpiration(token);

        // Then
        assertNotNull(expiration);
        // Expiration should be in the future
        assertTrue(expiration.getTime() > System.currentTimeMillis());
    }

    @Test
    @DisplayName("Should validate token with correct username")
    void shouldValidateToken_withCorrectUsername() {
        // Given
        String email = "test@std.iyte.edu.tr";
        String token = jwtUtil.generateToken(email);

        // When
        Boolean isValid = jwtUtil.validateToken(token, email);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should invalidate token with wrong username")
    void shouldInvalidateToken_withWrongUsername() {
        // Given
        String token = jwtUtil.generateToken("test@std.iyte.edu.tr");

        // When
        Boolean isValid = jwtUtil.validateToken(token, "other@std.iyte.edu.tr");

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should invalidate expired token")
    void shouldInvalidateExpiredToken() throws Exception {
        // Set negative expiration to make token expire immediately
        Field expirationField = JwtUtil.class.getDeclaredField("expiration");
        expirationField.setAccessible(true);
        expirationField.set(jwtUtil, -1L); // Past expiration

        String token = jwtUtil.generateToken("test@std.iyte.edu.tr");

        // When & Then - expect specific ExpiredJwtException
        assertThrows(
                ExpiredJwtException.class,
                () -> jwtUtil.validateToken(token, "test@std.iyte.edu.tr"));
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void shouldGenerateDifferentTokens() {
        // When
        String token1 = jwtUtil.generateToken("user1@std.iyte.edu.tr");
        String token2 = jwtUtil.generateToken("user2@std.iyte.edu.tr");

        // Then
        assertNotEquals(token1, token2);
    }
}
