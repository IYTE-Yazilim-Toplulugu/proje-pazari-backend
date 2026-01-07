package com.iyte_yazilim.proje_pazari.application.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;

@SpringBootTest
class MessageServiceTest {

    @Autowired
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        // Reset to default locale before each test
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    @DisplayName("Should return Turkish message when locale is Turkish")
    void shouldReturnTurkishMessage_whenLocaleIsTurkish() {
        // Given
        LocaleContextHolder.setLocale(Locale.forLanguageTag("tr"));

        // When
        String message = messageService.getMessage("auth.login.success");

        // Then
        assertEquals("Giriş başarılı", message);
    }

    @Test
    @DisplayName("Should return English message when locale is English")
    void shouldReturnEnglishMessage_whenLocaleIsEnglish() {
        // Given
        LocaleContextHolder.setLocale(Locale.forLanguageTag("en"));

        // When
        String message = messageService.getMessage("auth.login.success");

        // Then
        assertEquals("Login successful", message);
    }

    @Test
    @DisplayName("Should return Turkish message by default when no locale specified")
    void shouldReturnTurkishMessage_byDefault() {
        // When
        String message = messageService.getMessage("auth.login.success");

        // Then
        assertEquals("Giriş başarılı", message);
    }

    @Test
    @DisplayName("Should return message with parameters in Turkish")
    void shouldReturnMessageWithParameters_inTurkish() {
        // Given
        LocaleContextHolder.setLocale(Locale.forLanguageTag("tr"));
        Object[] args = {"E-posta"};

        // When
        String message = messageService.getMessage("validation.field.required", args);

        // Then
        assertEquals("E-posta alanı zorunludur", message);
    }

    @Test
    @DisplayName("Should return message with parameters in English")
    void shouldReturnMessageWithParameters_inEnglish() {
        // Given
        LocaleContextHolder.setLocale(Locale.forLanguageTag("en"));
        Object[] args = {"Email"};

        // When
        String message = messageService.getMessage("validation.field.required", args);

        // Then
        assertEquals("Email field is required", message);
    }

    @Test
    @DisplayName("Should fallback to Turkish when unsupported locale is provided")
    void shouldFallbackToTurkish_whenUnsupportedLocale() {
        // Given
        LocaleContextHolder.setLocale(Locale.forLanguageTag("fr")); // French not supported

        // When
        String message = messageService.getMessage("auth.login.success");

        // Then
        assertEquals("Giriş başarılı", message); // Should fallback to Turkish (default)
    }

    @Test
    @DisplayName("Should return message with default when key not found")
    void shouldReturnDefault_whenKeyNotFound() {
        // Given
        LocaleContextHolder.setLocale(Locale.forLanguageTag("en"));
        String defaultMessage = "Default message";

        // When
        String message = messageService.getMessage("non.existent.key", defaultMessage);

        // Then
        assertEquals(defaultMessage, message);
    }

    @Test
    @DisplayName("Should handle multiple parameters correctly")
    void shouldHandleMultipleParameters() {
        // Given
        LocaleContextHolder.setLocale(Locale.forLanguageTag("en"));
        Object[] args = {"Password", "8", "100"};

        // When
        String message = messageService.getMessage("validation.field.size", args);

        // Then
        assertEquals("Password field must be between 8 and 100 characters", message);
    }

    @Test
    @DisplayName("Should handle validation messages for different locales")
    void shouldHandleValidationMessages() {
        // Turkish
        LocaleContextHolder.setLocale(Locale.forLanguageTag("tr"));
        String turkishMessage = messageService.getMessage("validation.email.invalid");
        assertEquals("Geçersiz e-posta adresi", turkishMessage);

        // English
        LocaleContextHolder.setLocale(Locale.forLanguageTag("en"));
        String englishMessage = messageService.getMessage("validation.email.invalid");
        assertEquals("Invalid email address", englishMessage);
    }

    @Test
    @DisplayName("Should handle project messages correctly")
    void shouldHandleProjectMessages() {
        // Turkish
        LocaleContextHolder.setLocale(Locale.forLanguageTag("tr"));
        String turkishMessage = messageService.getMessage("project.created");
        assertEquals("Proje başarıyla oluşturuldu", turkishMessage);

        // English
        LocaleContextHolder.setLocale(Locale.forLanguageTag("en"));
        String englishMessage = messageService.getMessage("project.created");
        assertEquals("Project created successfully", englishMessage);
    }

    @Test
    @DisplayName("Should handle error messages correctly")
    void shouldHandleErrorMessages() {
        // Turkish
        LocaleContextHolder.setLocale(Locale.forLanguageTag("tr"));
        String turkishMessage = messageService.getMessage("error.unauthorized");
        assertEquals("Yetkisiz erişim", turkishMessage);

        // English
        LocaleContextHolder.setLocale(Locale.forLanguageTag("en"));
        String englishMessage = messageService.getMessage("error.unauthorized");
        assertEquals("Unauthorized access", englishMessage);
    }
}