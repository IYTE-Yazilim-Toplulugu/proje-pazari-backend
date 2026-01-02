package com.iyte_yazilim.proje_pazari.presentation.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String testSecret =
            "test-secret-key-for-testing-purposes-must-be-at-least-256-bits-long-for-hs256-algorithm";
    private final Long testExpiration = 86400000L;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", testExpiration);
    }

    @Test
    void testTokenGenerationWithNewClaims() {
        // Given
        String userId = "01HQZX9K2M3N4P5Q6R7S8T9V0W";
        String email = "test@example.com";
        String role = "USER";

        // When
        String token = jwtUtil.generateToken(userId, email, role);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testExtractUserId() {
        // Given
        String userId = "01HQZX9K2M3N4P5Q6R7S8T9V0W";
        String email = "test@example.com";
        String role = "USER";
        String token = jwtUtil.generateToken(userId, email, role);

        // When
        String extractedUserId = jwtUtil.extractUserId(token);

        // Then
        assertEquals(userId, extractedUserId);
    }

    @Test
    void testExtractEmail() {
        // Given
        String userId = "01HQZX9K2M3N4P5Q6R7S8T9V0W";
        String email = "test@example.com";
        String role = "USER";
        String token = jwtUtil.generateToken(userId, email, role);

        // When
        String extractedEmail = jwtUtil.extractEmail(token);

        // Then
        assertEquals(email, extractedEmail);
    }

    @Test
    void testExtractRole() {
        // Given
        String userId = "01HQZX9K2M3N4P5Q6R7S8T9V0W";
        String email = "test@example.com";
        String role = "USER";
        String token = jwtUtil.generateToken(userId, email, role);

        // When
        String extractedRole = jwtUtil.extractRole(token);

        // Then
        assertEquals(role, extractedRole);
    }

    @Test
    void testTokenValidation() {
        // Given
        String userId = "01HQZX9K2M3N4P5Q6R7S8T9V0W";
        String email = "test@example.com";
        String role = "USER";
        String token = jwtUtil.generateToken(userId, email, role);

        // When
        Boolean isValid = jwtUtil.validateToken(token, email);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testTokenValidationWithWrongEmail() {
        // Given
        String userId = "01HQZX9K2M3N4P5Q6R7S8T9V0W";
        String email = "test@example.com";
        String role = "USER";
        String token = jwtUtil.generateToken(userId, email, role);

        // When
        Boolean isValid = jwtUtil.validateToken(token, "wrong@example.com");

        // Then
        assertFalse(isValid);
    }

    @Test
    void testExtractUsername() {
        // Given
        String userId = "01HQZX9K2M3N4P5Q6R7S8T9V0W";
        String email = "test@example.com";
        String role = "USER";
        String token = jwtUtil.generateToken(userId, email, role);

        // When
        String extractedUsername = jwtUtil.extractUsername(token);

        // Then
        assertEquals(email, extractedUsername);
    }

    @Test
    void testAdminRoleToken() {
        // Given
        String userId = "01HQZX9K2M3N4P5Q6R7S8T9V0W";
        String email = "admin@example.com";
        String role = "ADMIN";

        // When
        String token = jwtUtil.generateToken(userId, email, role);
        String extractedRole = jwtUtil.extractRole(token);

        // Then
        assertEquals("ADMIN", extractedRole);
    }

    @Test
    void testModeratorRoleToken() {
        // Given
        String userId = "01HQZX9K2M3N4P5Q6R7S8T9V0W";
        String email = "mod@example.com";
        String role = "MODERATOR";

        // When
        String token = jwtUtil.generateToken(userId, email, role);
        String extractedRole = jwtUtil.extractRole(token);

        // Then
        assertEquals("MODERATOR", extractedRole);
    }
}