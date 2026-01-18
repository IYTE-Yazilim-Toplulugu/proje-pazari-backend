package com.iyte_yazilim.proje_pazari.presentation.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JwtUtilTest {

    @Autowired private JwtUtil jwtUtil;

    @Test
    void shouldGenerateTokenWithAllClaims() {
        // Given
        String userId = "01HQXYZ123";
        String email = "test@std.iyte.edu.tr";
        String role = "USER";

        // When
        String token = jwtUtil.generateToken(userId, email, role);

        // Then
        assertEquals(userId, jwtUtil.extractUserId(token));
        assertEquals(email, jwtUtil.extractEmail(token));
        assertEquals(role, jwtUtil.extractRole(token));
    }

    @Test
    void shouldExtractUserPrincipal() {
        // Given
        String token = jwtUtil.generateToken("01HQXYZ123", "test@std.iyte.edu.tr", "USER");

        // When
        UserPrincipal principal = jwtUtil.extractUserPrincipal(token);

        // Then
        assertEquals("01HQXYZ123", principal.getUserId());
        assertEquals("test@std.iyte.edu.tr", principal.getEmail());
        assertEquals("USER", principal.getRole());
        assertTrue(
                principal.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void shouldValidateToken() {
        // Given
        String token = jwtUtil.generateToken("01HQXYZ123", "test@std.iyte.edu.tr", "USER");

        // When
        Boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void shouldExtractUsername() {
        // Given
        String email = "test@std.iyte.edu.tr";
        String token = jwtUtil.generateToken("01HQXYZ123", email, "USER");

        // When
        String extractedUsername = jwtUtil.extractUsername(token);

        // Then
        assertEquals(email, extractedUsername);
    }

    @Test
    void shouldHandleAdminRole() {
        // Given
        String token = jwtUtil.generateToken("01HQXYZ123", "admin@std.iyte.edu.tr", "ADMIN");

        // When
        UserPrincipal principal = jwtUtil.extractUserPrincipal(token);

        // Then
        assertEquals("ADMIN", principal.getRole());
        assertTrue(
                principal.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void shouldHandleModeratorRole() {
        // Given
        String token = jwtUtil.generateToken("01HQXYZ123", "mod@std.iyte.edu.tr", "MODERATOR");

        // When
        UserPrincipal principal = jwtUtil.extractUserPrincipal(token);

        // Then
        assertEquals("MODERATOR", principal.getRole());
        assertTrue(
                principal.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_MODERATOR")));
    }

    @Test
    void shouldHandleProjectOwnerRole() {
        // Given
        String token =
                jwtUtil.generateToken("01HQXYZ123", "owner@std.iyte.edu.tr", "PROJECT_OWNER");

        // When
        UserPrincipal principal = jwtUtil.extractUserPrincipal(token);

        // Then
        assertEquals("PROJECT_OWNER", principal.getRole());
        assertTrue(
                principal.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_PROJECT_OWNER")));
    }

    @Test
    void shouldDefaultToUserRoleWhenRoleClaimMissing() {
        // Given - generate token with old method that doesn't include role
        String token = jwtUtil.generateToken("test@std.iyte.edu.tr");

        // When
        String role = jwtUtil.extractRole(token);

        // Then
        assertEquals("APPLICANT", role);
    }
}
