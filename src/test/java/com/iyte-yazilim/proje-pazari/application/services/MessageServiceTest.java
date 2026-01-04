package com.iyte_yazilim.proje_pazari.application.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for MessageService localization functionality.
 *
 * <p>This test verifies that:
 * <ul>
 *   <li>Messages are correctly resolved from properties files</li>
 *   <li>Turkish (tr) locale works properly</li>
 *   <li>English (en) locale works properly</li>
 *   <li>Parameterized messages work correctly</li>
 *   <li>Fallback behavior works when messages are not found</li>
 *   <li>LocaleContextHolder is properly used for locale resolution</li>
 * </ul>
 */
@SpringBootTest
@DisplayName("MessageService Localization Tests")
class MessageServiceTest {

    @Autowired
    private MessageService messageService;

    // Store original locale to restore after tests
    private Locale originalLocale;

    @BeforeEach
    void setUp() {
        // Store the original locale
        originalLocale = LocaleContextHolder.getLocale();
    }

    @AfterEach
    void tearDown() {
        // Restore the original locale after each test
        LocaleContextHolder.setLocale(originalLocale);
    }

    // ==================== TURKISH (TR) LOCALE TESTS ====================

    @Test
    @DisplayName("Should resolve Turkish authentication success message")
    void shouldResolveTurkishAuthenticationSuccessMessage() {
        // Given: Turkish locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("tr"));

        // When: Getting authentication success message
        String message = messageService.getMessage("auth.login.success");

        // Then: Should return Turkish message
        assertThat(message).isEqualTo("Giriş başarılı");
    }

    @Test
    @DisplayName("Should resolve Turkish user not found message")
    void shouldResolveTurkishUserNotFoundMessage() {
        // Given: Turkish locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("tr"));

        // When: Getting user not found message
        String message = messageService.getMessage("user.not.found");

        // Then: Should return Turkish message
        assertThat(message).isEqualTo("Kullanıcı bulunamadı");
    }

    @Test
    @DisplayName("Should resolve Turkish validation error message")
    void shouldResolveTurkishValidationErrorMessage() {
        // Given: Turkish locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("tr"));

        // When: Getting email validation error message
        String message = messageService.getMessage("validation.email.invalid");

        // Then: Should return Turkish message
        assertThat(message).isEqualTo("Geçersiz e-posta adresi");
    }

    @Test
    @DisplayName("Should resolve Turkish project created message")
    void shouldResolveTurkishProjectCreatedMessage() {
        // Given: Turkish locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("tr"));

        // When: Getting project created message
        String message = messageService.getMessage("project.created.success");

        // Then: Should return Turkish message
        assertThat(message).isEqualTo("Proje başarıyla oluşturuldu");
    }

    @Test
    @DisplayName("Should resolve Turkish password changed message")
    void shouldResolveTurkishPasswordChangedMessage() {
        // Given: Turkish locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("tr"));

        // When: Getting password changed message
        String message = messageService.getMessage("user.password.changed");

        // Then: Should return Turkish message
        assertThat(message).isEqualTo("Şifre başarıyla değiştirildi");
    }

    // ==================== ENGLISH (EN) LOCALE TESTS ====================

    @Test
    @DisplayName("Should resolve English authentication success message")
    void shouldResolveEnglishAuthenticationSuccessMessage() {
        // Given: English locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("en"));

        // When: Getting authentication success message
        String message = messageService.getMessage("auth.login.success");

        // Then: Should return English message
        assertThat(message).isEqualTo("Login successful");
    }

    @Test
    @DisplayName("Should resolve English user not found message")
    void shouldResolveEnglishUserNotFoundMessage() {
        // Given: English locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("en"));

        // When: Getting user not found message
        String message = messageService.getMessage("user.not.found");

        // Then: Should return English message
        assertThat(message).isEqualTo("User not found");
    }

    @Test
    @DisplayName("Should resolve English validation error message")
    void shouldResolveEnglishValidationErrorMessage() {
        // Given: English locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("en"));

        // When: Getting email validation error message
        String message = messageService.getMessage("validation.email.invalid");

        // Then: Should return English message
        assertThat(message).isEqualTo("Invalid email address");
    }

    @Test
    @DisplayName("Should resolve English project created message")
    void shouldResolveEnglishProjectCreatedMessage() {
        // Given: English locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("en"));

        // When: Getting project created message
        String message = messageService.getMessage("project.created.success");

        // Then: Should return English message
        assertThat(message).isEqualTo("Project created successfully");
    }

    @Test
    @DisplayName("Should resolve English password changed message")
    void shouldResolveEnglishPasswordChangedMessage() {
        // Given: English locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("en"));

        // When: Getting password changed message
        String message = messageService.getMessage("user.password.changed");

        // Then: Should return English message
        assertThat(message).isEqualTo("Password changed successfully");
    }

    // ==================== PARAMETERIZED MESSAGE TESTS ====================

    @Test
    @DisplayName("Should resolve Turkish parameterized message with user ID")
    void shouldResolveTurkishParameterizedMessage() {
        // Given: Turkish locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("tr"));

        // When: Getting parameterized message
        String message = messageService.getMessage(
                "user.not.found.with.id",
                new Object[]{"12345"}
        );

        // Then: Should return Turkish message with parameter
        assertThat(message).isEqualTo("ID 12345 ile kullanıcı bulunamadı");
    }

    @Test
    @DisplayName("Should resolve English parameterized message with user ID")
    void shouldResolveEnglishParameterizedMessage() {
        // Given: English locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("en"));

        // When: Getting parameterized message
        String message = messageService.getMessage(
                "user.not.found.with.id",
                new Object[]{"12345"}
        );

        // Then: Should return English message with parameter
        assertThat(message).isEqualTo("User with ID 12345 not found");
    }

    @Test
    @DisplayName("Should resolve Turkish parameterized message with project owner ID")
    void shouldResolveTurkishParameterizedProjectOwnerMessage() {
        // Given: Turkish locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("tr"));

        // When: Getting parameterized message
        String message = messageService.getMessage(
                "project.owner.not.found",
                new Object[]{"ABC123"}
        );

        // Then: Should return Turkish message with parameter
        assertThat(message).isEqualTo("ID ABC123 olan proje sahibi bulunamadı");
    }

    @Test
    @DisplayName("Should resolve English parameterized message with project owner ID")
    void shouldResolveEnglishParameterizedProjectOwnerMessage() {
        // Given: English locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("en"));

        // When: Getting parameterized message
        String message = messageService.getMessage(
                "project.owner.not.found",
                new Object[]{"ABC123"}
        );

        // Then: Should return English message with parameter
        assertThat(message).isEqualTo("Owner with ID ABC123 not found");
    }

    // ==================== JAKARTA VALIDATION MESSAGES TESTS ====================

    @Test
    @DisplayName("Should resolve Turkish Jakarta NotBlank validation message")
    void shouldResolveTurkishJakartaNotBlankMessage() {
        // Given: Turkish locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("tr"));

        // When: Getting Jakarta validation message
        String message = messageService.getMessage("jakarta.validation.constraints.NotBlank.message");

        // Then: Should return Turkish message
        assertThat(message).isEqualTo("Bu alan boş bırakılamaz");
    }

    @Test
    @DisplayName("Should resolve English Jakarta NotBlank validation message")
    void shouldResolveEnglishJakartaNotBlankMessage() {
        // Given: English locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("en"));

        // When: Getting Jakarta validation message
        String message = messageService.getMessage("jakarta.validation.constraints.NotBlank.message");

        // Then: Should return English message
        assertThat(message).isEqualTo("This field cannot be blank");
    }

    @Test
    @DisplayName("Should resolve Turkish Jakarta Email validation message")
    void shouldResolveTurkishJakartaEmailMessage() {
        // Given: Turkish locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("tr"));

        // When: Getting Jakarta email validation message
        String message = messageService.getMessage("jakarta.validation.constraints.Email.message");

        // Then: Should return Turkish message
        assertThat(message).isEqualTo("Lütfen geçerli bir e-posta adresi girin");
    }

    @Test
    @DisplayName("Should resolve English Jakarta Email validation message")
    void shouldResolveEnglishJakartaEmailMessage() {
        // Given: English locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("en"));

        // When: Getting Jakarta email validation message
        String message = messageService.getMessage("jakarta.validation.constraints.Email.message");

        // Then: Should return English message
        assertThat(message).isEqualTo("Please enter a valid email address");
    }

    // ==================== FALLBACK BEHAVIOR TESTS ====================

    @Test
    @DisplayName("Should return message code as fallback when message not found in Turkish")
    void shouldReturnCodeAsFallbackInTurkish() {
        // Given: Turkish locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("tr"));

        // When: Getting non-existent message
        String message = messageService.getMessage("non.existent.message.key");

        // Then: Should return the code itself as fallback
        assertThat(message).isEqualTo("non.existent.message.key");
    }

    @Test
    @DisplayName("Should return message code as fallback when message not found in English")
    void shouldReturnCodeAsFallbackInEnglish() {
        // Given: English locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("en"));

        // When: Getting non-existent message
        String message = messageService.getMessage("non.existent.message.key");

        // Then: Should return the code itself as fallback
        assertThat(message).isEqualTo("non.existent.message.key");
    }

    @Test
    @DisplayName("Should return default message when provided and message not found")
    void shouldReturnDefaultMessageWhenProvided() {
        // Given: Turkish locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("tr"));

        // When: Getting non-existent message with default
        String message = messageService.getMessage(
                "non.existent.key",
                "Default fallback message"
        );

        // Then: Should return the provided default message
        assertThat(message).isEqualTo("Default fallback message");
    }

    // ==================== CONVENIENCE METHOD TESTS ====================

    @Test
    @DisplayName("Should resolve Turkish success message using convenience method")
    void shouldResolveTurkishSuccessMessageUsingConvenienceMethod() {
        // Given: Turkish locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("tr"));

        // When: Getting success message using convenience method
        String message = messageService.getSuccessMessage("user.registered.success");

        // Then: Should return Turkish message
        assertThat(message).isEqualTo("Kullanıcı başarıyla kaydedildi");
    }

    @Test
    @DisplayName("Should resolve English error message using convenience method")
    void shouldResolveEnglishErrorMessageUsingConvenienceMethod() {
        // Given: English locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("en"));

        // When: Getting error message using convenience method
        String message = messageService.getErrorMessage("error.unauthorized");

        // Then: Should return English message
        assertThat(message).isEqualTo("Unauthorized access");
    }

    // ==================== LOCALE CONTEXT HOLDER TESTS ====================

    @Test
    @DisplayName("Should get current locale from LocaleContextHolder")
    void shouldGetCurrentLocaleFromContextHolder() {
        // Given: English locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("en"));

        // When: Getting current locale
        Locale currentLocale = messageService.getCurrentLocale();

        // Then: Should return English locale
        assertThat(currentLocale.getLanguage()).isEqualTo("en");
    }

    @Test
    @DisplayName("Should check if message exists for Turkish locale")
    void shouldCheckIfMessageExistsForTurkishLocale() {
        // Given: Turkish locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("tr"));

        // When: Checking if message exists
        boolean exists = messageService.hasMessage("user.not.found");

        // Then: Should return true
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should check if message exists for English locale")
    void shouldCheckIfMessageExistsForEnglishLocale() {
        // Given: English locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("en"));

        // When: Checking if message exists
        boolean exists = messageService.hasMessage("user.not.found");

        // Then: Should return true
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false for non-existent message")
    void shouldReturnFalseForNonExistentMessage() {
        // Given: Turkish locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("tr"));

        // When: Checking if non-existent message exists
        boolean exists = messageService.hasMessage("this.message.does.not.exist");

        // Then: Should return false
        assertThat(exists).isFalse();
    }

    // ==================== LOCALE SWITCHING TESTS ====================

    @Test
    @DisplayName("Should resolve different messages when locale is switched")
    void shouldResolveDifferentMessagesWhenLocaleSwitched() {
        // Given: Start with Turkish locale
        LocaleContextHolder.setLocale(Locale.forLanguageTag("tr"));
        String turkishMessage = messageService.getMessage("auth.login.success");

        // When: Switch to English locale
        LocaleContextHolder.setLocale(Locale.forLanguageTag("en"));
        String englishMessage = messageService.getMessage("auth.login.success");

        // Then: Messages should be different
        assertThat(turkishMessage).isEqualTo("Giriş başarılı");
        assertThat(englishMessage).isEqualTo("Login successful");
        assertThat(turkishMessage).isNotEqualTo(englishMessage);
    }

    @Test
    @DisplayName("Should handle explicit locale parameter overriding LocaleContextHolder")
    void shouldHandleExplicitLocaleParameter() {
        // Given: LocaleContextHolder is set to Turkish
        LocaleContextHolder.setLocale(Locale.forLanguageTag("tr"));

        // When: Getting message with explicit English locale
        String message = messageService.getMessage(
                "user.not.found",
                null,
                Locale.forLanguageTag("en")
        );

        // Then: Should return English message (explicit locale takes precedence)
        assertThat(message).isEqualTo("User not found");
    }

    // ==================== SPECIAL CHARACTERS TEST (TURKISH) ====================

    @Test
    @DisplayName("Should correctly handle Turkish special characters (ç, ğ, ı, ö, ş, ü)")
    void shouldHandleTurkishSpecialCharacters() {
        // Given: Turkish locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("tr"));

        // When: Getting message with Turkish special characters
        String message = messageService.getMessage("user.password.changed");

        // Then: Should correctly display Turkish characters
        assertThat(message)
                .isEqualTo("Şifre başarıyla değiştirildi")
                .contains("Ş", "ı", "ş", "ğ");
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("Should handle null locale gracefully and use default")
    void shouldHandleNullLocaleGracefully() {
        // Given: Set locale to null (should fall back to default Turkish)
        LocaleContextHolder.setLocale(null);

        // When: Getting message
        String message = messageService.getMessage("user.not.found");

        // Then: Should still work (using default locale)
        assertThat(message).isNotNull();
    }

    @Test
    @DisplayName("Should handle empty message code gracefully")
    void shouldHandleEmptyMessageCodeGracefully() {
        // Given: Turkish locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("tr"));

        // When: Getting message with empty code
        String message = messageService.getMessage("");

        // Then: Should return empty string or handle gracefully
        assertThat(message).isNotNull();
    }

    @Test
    @DisplayName("Should handle multiple parameters in message")
    void shouldHandleMultipleParametersInMessage() {
        // Given: English locale is set
        LocaleContextHolder.setLocale(Locale.forLanguageTag("en"));

        // When: Getting message with multiple parameters (if such message exists)
        // Note: Using validation.field.size which has {0}, {1}, {2} parameters
        String message = messageService.getMessage(
                "validation.field.size",
                new Object[]{"Username", "3", "20"}
        );

        // Then: Should correctly substitute all parameters
        assertThat(message)
                .contains("Username")
                .contains("3")
                .contains("20");
    }
}