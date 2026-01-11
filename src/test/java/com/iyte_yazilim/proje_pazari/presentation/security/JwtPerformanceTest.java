package com.iyte_yazilim.proje_pazari.presentation.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(
        properties = {
            "spring.data.elasticsearch.enabled=false",
            "spring.data.elasticsearch.repositories.enabled=false",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration,org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration,org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration"
        })
class JwtPerformanceTest {

    @Autowired private JwtUtil jwtUtil;

    @Test
    void shouldGenerateTokensQuickly() {
        // Given
        String userId = "01HQXYZ123";
        String email = "test@std.iyte.edu.tr";
        String role = "USER";
        int iterations = 1000;

        // When
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            jwtUtil.generateToken(userId, email, role);
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Then
        System.out.println("Generated " + iterations + " tokens in " + duration + "ms");
        System.out.println("Average time per token: " + (duration / (double) iterations) + "ms");
        assertTrue(
                duration < 5000, "Token generation should be fast (< 5 seconds for 1000 tokens)");
    }

    @Test
    void shouldValidateTokensQuickly() {
        // Given
        String token = jwtUtil.generateToken("01HQXYZ123", "test@std.iyte.edu.tr", "USER");
        int iterations = 1000;

        // When
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            jwtUtil.validateToken(token);
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Then
        System.out.println("Validated " + iterations + " tokens in " + duration + "ms");
        System.out.println(
                "Average time per validation: " + (duration / (double) iterations) + "ms");
        assertTrue(
                duration < 5000, "Token validation should be fast (< 5 seconds for 1000 tokens)");
    }

    @Test
    void shouldExtractClaimsQuickly() {
        // Given
        String token = jwtUtil.generateToken("01HQXYZ123", "test@std.iyte.edu.tr", "USER");
        int iterations = 1000;

        // When
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            jwtUtil.extractUserId(token);
            jwtUtil.extractEmail(token);
            jwtUtil.extractRole(token);
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Then
        System.out.println("Extracted claims " + iterations + " times in " + duration + "ms");
        System.out.println(
                "Average time per extraction: " + (duration / (double) iterations) + "ms");
        assertTrue(
                duration < 5000,
                "Claims extraction should be fast (< 5 seconds for 1000 iterations)");
    }

    @Test
    void shouldExtractUserPrincipalQuickly() {
        // Given
        String token = jwtUtil.generateToken("01HQXYZ123", "test@std.iyte.edu.tr", "USER");
        int iterations = 1000;

        // When
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            jwtUtil.extractUserPrincipal(token);
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Then
        System.out.println(
                "Extracted UserPrincipal " + iterations + " times in " + duration + "ms");
        System.out.println(
                "Average time per extraction: " + (duration / (double) iterations) + "ms");
        assertTrue(
                duration < 5000,
                "UserPrincipal extraction should be fast (< 5 seconds for 1000 iterations)");
    }
}
