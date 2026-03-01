package com.iyte_yazilim.proje_pazari.application.commands.registerUser;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RegisterUserValidatorTest {

    private final RegisterUserValidator validator = new RegisterUserValidator();

    @Test
    @DisplayName("Should return empty array for valid command")
    void shouldReturnEmpty_whenCommandIsValid() {
        // Given
        RegisterUserCommand command =
                new RegisterUserCommand("test@std.iyte.edu.tr", "password123", "John", "Doe");

        // When
        String[] errors = validator.validate(command);

        // Then
        assertNotNull(errors);
        assertEquals(0, errors.length);
    }

    @Test
    @DisplayName("Should return errors for non-IYTE email - validator checks IYTE domain")
    void shouldReturnErrors_whenNonIyteEmail() {
        // Given - RegisterUserValidator now validates IYTE email via IyteEmail value
        // object
        RegisterUserCommand command = new RegisterUserCommand("test@gmail.com", "short", "A", "B");

        // When
        String[] errors = validator.validate(command);

        // Then
        assertNotNull(errors);
        assertTrue(errors.length > 0, "Should return validation errors for non-IYTE email");
    }
}
