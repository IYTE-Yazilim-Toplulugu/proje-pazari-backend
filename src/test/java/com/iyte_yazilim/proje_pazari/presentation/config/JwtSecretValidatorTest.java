package com.iyte_yazilim.proje_pazari.presentation.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtSecretValidatorTest {

    @Test
    void shouldThrowWhenSecretContainsPlaceholderIndicator() {
        JwtSecretValidator validator = new JwtSecretValidator();
        ReflectionTestUtils.setField(
                validator, "jwtSecret", "change-this-in-production-insecure-default-placeholder");

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, validator::validateJwtSecret);
        assertTrue(ex.getMessage().contains("JWT_SECRET"));
    }

    @Test
    void shouldThrowWhenSecretIsShorterThan32Characters() {
        JwtSecretValidator validator = new JwtSecretValidator();
        ReflectionTestUtils.setField(validator, "jwtSecret", "too-short");

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, validator::validateJwtSecret);
        assertTrue(ex.getMessage().contains("32"));
    }

    @Test
    void shouldPassWhenSecretIsValidAndLongEnough() {
        JwtSecretValidator validator = new JwtSecretValidator();
        ReflectionTestUtils.setField(
                validator,
                "jwtSecret",
                "a-secure-random-secret-that-is-at-least-32-characters-long");

        assertDoesNotThrow(validator::validateJwtSecret);
    }
}
