package com.iyte_yazilim.proje_pazari.presentation.security;

import static org.junit.jupiter.api.Assertions.*;

import com.iyte_yazilim.proje_pazari.presentation.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(
        properties = {
            "spring.data.elasticsearch.enabled=false",
            "spring.data.elasticsearch.repositories.enabled=false",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration,org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration,org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
        })
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
    void generateToken_shouldIncludeAllRequiredClaims() {
        // Given
        String token = jwtUtil.generateToken("user-123", "test@std.iyte.edu.tr", "APPLICANT");

        // When
        UserPrincipal principal = jwtUtil.extractUserPrincipal(token);

        // Then
        assertEquals("user-123", principal.getUserId());
        assertEquals("test@std.iyte.edu.tr", principal.getEmail());
        assertEquals("APPLICANT", principal.getRole());
    }
}
