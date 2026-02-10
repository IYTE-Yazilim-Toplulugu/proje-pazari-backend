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
        RegisterUserCommand command = new RegisterUserCommand("test@std.iyte.edu.tr", "password123", "John", "Doe");

        // When
        String[] errors = validator.validate(command);

        // Then
        assertNotNull(errors);
        assertEquals(0, errors.length);
    }

    @Test
    @DisplayName("Should return empty array - validation delegated to Jakarta annotations")
    void shouldReturnEmpty_validationDelegatedToJakarta() {
        // Given - even with unusual input, this validator delegates to Jakarta
        RegisterUserCommand command = new RegisterUserCommand("test@gmail.com", "short", "A", "B");

        // When
        String[] errors = validator.validate(command);

        // Then
        assertNotNull(errors);
        assertEquals(0, errors.length);
    }
}
