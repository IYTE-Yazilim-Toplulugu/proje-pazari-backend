package com.iyte_yazilim.proje_pazari.application.commands.createProject;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CreateProjectValidatorTest {

    private final CreateProjectValidator validator = new CreateProjectValidator();

    @Test
    @DisplayName("Should return no errors for valid command")
    void shouldReturnNoErrors_whenCommandIsValid() {
        // Given
        CreateProjectCommand command = new CreateProjectCommand(
                "Test Project",
                "A valid project description",
                "owner-123",
                new String[] {},
                new String[] { "java" },
                5,
                new String[] { "Java" },
                "Software Development",
                LocalDateTime.now().plusDays(30));

        // When
        String[] errors = validator.validate(command);

        // Then
        assertNotNull(errors);
        assertEquals(0, errors.length);
    }

    @Test
    @DisplayName("Should return error when project name is null")
    void shouldReturnError_whenProjectNameIsNull() {
        // Given
        CreateProjectCommand command = new CreateProjectCommand(
                null,
                "A valid description",
                "owner-123",
                new String[] {},
                new String[] {},
                5,
                new String[] {},
                null,
                null);

        // When
        String[] errors = validator.validate(command);

        // Then
        assertTrue(errors.length > 0);
        boolean hasNameError = false;
        for (String error : errors) {
            if (error.contains("Project name is required")) {
                hasNameError = true;
                break;
            }
        }
        assertTrue(hasNameError);
    }

    @Test
    @DisplayName("Should return error when project name is empty")
    void shouldReturnError_whenProjectNameIsEmpty() {
        // Given
        CreateProjectCommand command = new CreateProjectCommand(
                "",
                "A valid description",
                "owner-123",
                new String[] {},
                new String[] {},
                5,
                new String[] {},
                null,
                null);

        // When
        String[] errors = validator.validate(command);

        // Then
        assertTrue(errors.length > 0);
        boolean hasNameError = false;
        for (String error : errors) {
            if (error.contains("Project name is required")) {
                hasNameError = true;
                break;
            }
        }
        assertTrue(hasNameError);
    }

    @Test
    @DisplayName("Should return error when owner ID is null")
    void shouldReturnError_whenOwnerIdIsNull() {
        // Given
        CreateProjectCommand command = new CreateProjectCommand(
                "Test Project",
                "A valid description",
                null,
                new String[] {},
                new String[] {},
                5,
                new String[] {},
                null,
                null);

        // When
        String[] errors = validator.validate(command);

        // Then
        assertTrue(errors.length > 0);
        boolean hasOwnerError = false;
        for (String error : errors) {
            if (error.contains("Owner ID is required")) {
                hasOwnerError = true;
                break;
            }
        }
        assertTrue(hasOwnerError);
    }

    @Test
    @DisplayName("Should return error when owner ID is empty")
    void shouldReturnError_whenOwnerIdIsEmpty() {
        // Given
        CreateProjectCommand command = new CreateProjectCommand(
                "Test Project",
                "A valid description",
                "",
                new String[] {},
                new String[] {},
                5,
                new String[] {},
                null,
                null);

        // When
        String[] errors = validator.validate(command);

        // Then
        assertTrue(errors.length > 0);
        boolean hasOwnerError = false;
        for (String error : errors) {
            if (error.contains("Owner ID is required")) {
                hasOwnerError = true;
                break;
            }
        }
        assertTrue(hasOwnerError);
    }

    @Test
    @DisplayName("Should return multiple errors when both name and owner ID are missing")
    void shouldReturnMultipleErrors_whenBothFieldsAreMissing() {
        // Given
        CreateProjectCommand command = new CreateProjectCommand(
                null,
                "A valid description",
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        // When
        String[] errors = validator.validate(command);

        // Then
        assertEquals(2, errors.length);
    }
}
