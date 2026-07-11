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
        CreateProjectCommand command =
                new CreateProjectCommand(
                        "Test Project",
                        "A valid project description",
                        "A concise summary of the test project",
                        "owner-123",
                        5,
                        new String[] {"Java"},
                        "Software Development",
                        LocalDateTime.now().plusDays(30));

        String[] errors = validator.validate(command);

        assertNotNull(errors);
        assertEquals(0, errors.length);
    }

    @Test
    @DisplayName("Should return no errors when deadline is null")
    void shouldReturnNoErrors_whenDeadlineIsNull() {
        CreateProjectCommand command =
                new CreateProjectCommand(
                        "Test Project",
                        "A valid description",
                        "A concise summary of the test project",
                        "owner-123",
                        5,
                        new String[] {},
                        null,
                        null);

        String[] errors = validator.validate(command);

        assertNotNull(errors);
        assertEquals(0, errors.length);
    }

    @Test
    @DisplayName("Should return error when deadline is in the past")
    void shouldReturnError_whenDeadlineIsInThePast() {
        CreateProjectCommand command =
                new CreateProjectCommand(
                        "Test Project",
                        "A valid description",
                        "A concise summary of the test project",
                        "owner-123",
                        5,
                        new String[] {},
                        null,
                        LocalDateTime.now().minusDays(1));

        String[] errors = validator.validate(command);

        assertEquals(1, errors.length);
        assertTrue(errors[0].contains("Deadline cannot be in the past"));
    }

    @Test
    @DisplayName("Should return no errors when deadline is in the future")
    void shouldReturnNoErrors_whenDeadlineIsInTheFuture() {
        CreateProjectCommand command =
                new CreateProjectCommand(
                        "Test Project",
                        "A valid description",
                        "A concise summary of the test project",
                        "owner-123",
                        5,
                        new String[] {},
                        null,
                        LocalDateTime.now().plusDays(30));

        String[] errors = validator.validate(command);

        assertNotNull(errors);
        assertEquals(0, errors.length);
    }
}
