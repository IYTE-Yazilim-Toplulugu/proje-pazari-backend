package com.iyte_yazilim.proje_pazari.application.commands.updateUserProfile;

import static org.junit.jupiter.api.Assertions.*;

import com.github.f4b6a3.ulid.Ulid;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UpdateUserProfileValidatorTest {

    private final UpdateUserProfileValidator validator = new UpdateUserProfileValidator();

    @Test
    @DisplayName("Should return no errors for valid command")
    void shouldReturnNoErrors_whenCommandIsValid() {
        UpdateUserProfileCommand command =
                new UpdateUserProfileCommand(
                        Ulid.fast().toString(),
                        "John",
                        "Doe",
                        "desc",
                        "https://www.linkedin.com/in/johndoe",
                        "https://github.com/johndoe",
                        "en");

        String[] errors = validator.validate(command);

        assertNotNull(errors);
        assertEquals(0, errors.length);
    }

    @Test
    @DisplayName("Should return no errors when preferredLanguage is null")
    void shouldReturnNoErrors_whenPreferredLanguageIsNull() {
        UpdateUserProfileCommand command =
                new UpdateUserProfileCommand(
                        Ulid.fast().toString(), "John", "Doe", null, null, null, null);

        String[] errors = validator.validate(command);

        assertNotNull(errors);
        assertEquals(0, errors.length);
    }

    @Test
    @DisplayName("Should return error for unsupported preferredLanguage")
    void shouldReturnError_whenPreferredLanguageIsUnsupported() {
        UpdateUserProfileCommand command =
                new UpdateUserProfileCommand(
                        Ulid.fast().toString(), "John", "Doe", null, null, null, "es");

        String[] errors = validator.validate(command);

        assertNotNull(errors);
        assertEquals(1, errors.length);
        assertTrue(errors[0].contains("Invalid preferred language"));
    }

    @Test
    @DisplayName("Should return error for blank preferredLanguage")
    void shouldReturnError_whenPreferredLanguageIsBlank() {
        UpdateUserProfileCommand command =
                new UpdateUserProfileCommand(
                        Ulid.fast().toString(), "John", "Doe", null, null, null, "");

        String[] errors = validator.validate(command);

        assertNotNull(errors);
        assertEquals(1, errors.length);
        assertTrue(errors[0].contains("Invalid preferred language"));
    }

    @Test
    @DisplayName("Should return error for oversized preferredLanguage")
    void shouldReturnError_whenPreferredLanguageIsTooLong() {
        UpdateUserProfileCommand command =
                new UpdateUserProfileCommand(
                        Ulid.fast().toString(), "John", "Doe", null, null, null, "english");

        String[] errors = validator.validate(command);

        assertNotNull(errors);
        assertEquals(1, errors.length);
        assertTrue(errors[0].contains("Invalid preferred language"));
    }

    @Test
    @DisplayName("Should accept 'tr' as preferredLanguage")
    void shouldReturnNoErrors_whenPreferredLanguageIsTr() {
        UpdateUserProfileCommand command =
                new UpdateUserProfileCommand(
                        Ulid.fast().toString(), "John", "Doe", null, null, null, "tr");

        String[] errors = validator.validate(command);

        assertNotNull(errors);
        assertEquals(0, errors.length);
    }

    @Test
    @DisplayName("Should return error for invalid LinkedIn URL")
    void shouldReturnError_whenLinkedInUrlIsInvalid() {
        UpdateUserProfileCommand command =
                new UpdateUserProfileCommand(
                        Ulid.fast().toString(),
                        null,
                        null,
                        null,
                        "http://linkedin.com/in/johndoe",
                        null,
                        null);

        String[] errors = validator.validate(command);

        assertNotNull(errors);
        assertEquals(1, errors.length);
        assertTrue(errors[0].contains("LinkedIn"));
    }

    @Test
    @DisplayName("Should return error for invalid GitHub URL")
    void shouldReturnError_whenGithubUrlIsInvalid() {
        UpdateUserProfileCommand command =
                new UpdateUserProfileCommand(
                        Ulid.fast().toString(),
                        null,
                        null,
                        null,
                        null,
                        "http://github.com/johndoe",
                        null);

        String[] errors = validator.validate(command);

        assertNotNull(errors);
        assertEquals(1, errors.length);
        assertTrue(errors[0].contains("GitHub"));
    }

    @Test
    @DisplayName("Should return multiple errors when both URL and language are invalid")
    void shouldReturnMultipleErrors_whenMultipleFieldsInvalid() {
        UpdateUserProfileCommand command =
                new UpdateUserProfileCommand(
                        Ulid.fast().toString(),
                        null,
                        null,
                        null,
                        "invalid-url",
                        "invalid-url",
                        "invalid");

        String[] errors = validator.validate(command);

        assertNotNull(errors);
        assertEquals(3, errors.length);
    }
}
