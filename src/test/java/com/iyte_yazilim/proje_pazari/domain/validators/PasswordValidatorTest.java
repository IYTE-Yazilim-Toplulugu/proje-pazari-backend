package com.iyte_yazilim.proje_pazari.domain.validators;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PasswordValidatorTest {

    private PasswordValidator validator;

    @Mock private ConstraintValidatorContext context;

    @Mock private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        validator = new PasswordValidator();
        validator.initialize(null);

        // Setup mocks (use lenient to avoid unnecessary stubbing errors)
        lenient()
                .when(context.buildConstraintViolationWithTemplate(anyString()))
                .thenReturn(violationBuilder);
        lenient().when(violationBuilder.addConstraintViolation()).thenReturn(context);
    }

    @Test
    @DisplayName("Should accept valid password with all requirements")
    void shouldAcceptValidPassword() {
        // Given
        String validPassword = "Password123!";

        // When
        boolean result = validator.isValid(validPassword, context);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Should accept password with all allowed special characters")
    void shouldAcceptPasswordWithAllSpecialChars() {
        // Given - Test each special character
        String[] validPasswords = {
            "Password1@",
            "Password1$",
            "Password1!",
            "Password1%",
            "Password1*",
            "Password1?",
            "Password1&"
        };

        // When & Then
        for (String password : validPasswords) {
            assertTrue(
                    validator.isValid(password, context),
                    "Should accept password with: " + password);
        }
    }

    @Test
    @DisplayName("Should accept null password (handled by @NotBlank)")
    void shouldAcceptNullPassword() {
        // Note: Null validation is handled by @NotBlank annotation
        // PasswordValidator returns true to allow @NotBlank to handle it

        // When
        boolean result = validator.isValid(null, context);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Should accept blank password (handled by @NotBlank)")
    void shouldAcceptBlankPassword() {
        // Note: Blank validation is handled by @NotBlank annotation
        // PasswordValidator returns true to allow @NotBlank to handle it

        // When
        boolean result = validator.isValid("", context);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Should reject password shorter than 8 characters")
    void shouldRejectShortPassword() {
        // Given
        String shortPassword = "Pass1!";

        // When
        boolean result = validator.isValid(shortPassword, context);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should reject password without uppercase letter")
    void shouldRejectPasswordWithoutUppercase() {
        // Given
        String password = "password123!";

        // When
        boolean result = validator.isValid(password, context);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should reject password without lowercase letter")
    void shouldRejectPasswordWithoutLowercase() {
        // Given
        String password = "PASSWORD123!";

        // When
        boolean result = validator.isValid(password, context);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should reject password without digit")
    void shouldRejectPasswordWithoutDigit() {
        // Given
        String password = "Password!";

        // When
        boolean result = validator.isValid(password, context);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should reject password without special character")
    void shouldRejectPasswordWithoutSpecialChar() {
        // Given
        String password = "Password123";

        // When
        boolean result = validator.isValid(password, context);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should accept password with exactly 8 characters")
    void shouldAcceptPasswordWithExactly8Characters() {
        // Given
        String password = "Pass123!";

        // When
        boolean result = validator.isValid(password, context);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Should accept long password")
    void shouldAcceptLongPassword() {
        // Given
        String longPassword = "VeryLongPassword123!WithManyCharacters";

        // When
        boolean result = validator.isValid(longPassword, context);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Should reject password with invalid special character")
    void shouldRejectPasswordWithInvalidSpecialChar() {
        // Given - using # which is not in allowed list
        String password = "Password123#";

        // When
        boolean result = validator.isValid(password, context);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should reject password with only numbers and special chars")
    void shouldRejectPasswordWithoutLetters() {
        // Given
        String password = "12345678!@#$";

        // When
        boolean result = validator.isValid(password, context);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should accept password with multiple uppercase and lowercase")
    void shouldAcceptPasswordWithMultipleCases() {
        // Given
        String password = "PASSword123!";

        // When
        boolean result = validator.isValid(password, context);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Should accept password with multiple digits")
    void shouldAcceptPasswordWithMultipleDigits() {
        // Given
        String password = "Password1234567890!";

        // When
        boolean result = validator.isValid(password, context);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Should accept password with multiple special characters")
    void shouldAcceptPasswordWithMultipleSpecialChars() {
        // Given
        String password = "Password123!@$";

        // When
        boolean result = validator.isValid(password, context);

        // Then
        assertTrue(result);
    }
}
