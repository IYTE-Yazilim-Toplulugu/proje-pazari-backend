package com.iyte_yazilim.proje_pazari.presentation.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtPerformanceTest {

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
    void testTokenGenerationPerformance() {
        // Given
        String userId = "01HQZX9K2M3N4P5Q6R7S8T9V0W";
        String email = "test@example.com";
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
        assertTrue(duration < 5000, "Token generation should be fast");
    }

    @Test
    void testTokenValidationPerformance() {
        // Given
        String userId = "01HQZX9K2M3N4P5Q6R7S8T9V0W";
        String email = "test@example.com";
        String role = "USER";
        String token = jwtUtil.generateToken(userId, email, role);
        int iterations = 1000;

        // When
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            jwtUtil.validateToken(token, email);
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Then
        System.out.println("Validated " + iterations + " tokens in " + duration + "ms");
        System.out.println(
                "Average time per validation: " + (duration / (double) iterations) + "ms");
        assertTrue(duration < 5000, "Token validation should be fast");
    }

    @Test
    void testClaimsExtractionPerformance() {
        // Given
        String userId = "01HQZX9K2M3N4P5Q6R7S8T9V0W";
        String email = "test@example.com";
        String role = "USER";
        String token = jwtUtil.generateToken(userId, email, role);
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
        assertTrue(duration < 5000, "Claims extraction should be fast");
    }

    @Test
    void testOldVsNewTokenGeneration() {
        // Given
        String userId = "01HQZX9K2M3N4P5Q6R7S8T9V0W";
        String email = "test@example.com";
        String role = "USER";
        int iterations = 1000;

        // Test old method (single parameter)
        long oldStartTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            jwtUtil.generateToken(email);
        }
        long oldEndTime = System.currentTimeMillis();
        long oldDuration = oldEndTime - oldStartTime;

        // Test new method (with claims)
        long newStartTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            jwtUtil.generateToken(userId, email, role);
        }
        long newEndTime = System.currentTimeMillis();
        long newDuration = newEndTime - newStartTime;

        // Then
        System.out.println("Old method: " + oldDuration + "ms");
        System.out.println("New method: " + newDuration + "ms");
        System.out.println("Difference: " + (newDuration - oldDuration) + "ms");

        // The difference should be minimal (claims don't significantly impact performance)
        assertTrue(
                Math.abs(newDuration - oldDuration) < 1000,
                "Performance difference should be minimal");
    }
}