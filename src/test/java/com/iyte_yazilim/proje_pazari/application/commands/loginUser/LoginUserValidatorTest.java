package com.iyte_yazilim.proje_pazari.application.commands.loginUser;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoginUserValidatorTest {

    private final LoginUserValidator validator = new LoginUserValidator();

    @Test
    @DisplayName("Should return empty array for valid command")
    void shouldReturnEmpty_whenCommandIsValid() {
        // Given
        LoginUserCommand command = new LoginUserCommand("test@std.iyte.edu.tr", "password123");

        // When
        String[] errors = validator.validate(command);

        // Then
        assertNotNull(errors);
        assertEquals(0, errors.length);
    }

    @Test
    @DisplayName("Should return empty array - validation delegated to Jakarta annotations")
    void shouldReturnEmpty_validationDelegatedToJakarta() {
        // Given
        LoginUserCommand command = new LoginUserCommand("", "");

        // When
        String[] errors = validator.validate(command);

        // Then
        assertNotNull(errors);
        assertEquals(0, errors.length);
    }
}
