package com.iyte_yazilim.proje_pazari.presentation.config;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Configuration for Jakarta Bean Validation with i18n support.
 *
 * <p>This configuration ensures that validation messages (@NotBlank, @Email, @Size, etc.)
 * are resolved from the existing message properties files (messages.properties,
 * messages_tr.properties, messages_en.properties) and respect the user's locale preference.
 *
 * <p><strong>How it works:</strong>
 * <ol>
 *   <li>LocalValidatorFactoryBean is configured with the existing MessageSource</li>
 *   <li>Validation messages use keys like: jakarta.validation.constraints.NotBlank.message</li>
 *   <li>Locale is resolved automatically via LocaleInterceptor (Accept-Language header or user preference)</li>
 *   <li>Custom messages in annotations can reference message keys: @NotBlank(message = "{user.email.required}")</li>
 * </ol>
 *
 * <p><strong>Usage in annotations:</strong>
 * <pre>
 * // Standard Jakarta validation key (auto-resolved from messages.properties)
 * {@code @NotBlank(message = "{jakarta.validation.constraints.NotBlank.message}")}
 * String email;
 *
 * // Custom message key
 * {@code @NotBlank(message = "{user.email.required}")}
 * String email;
 *
 * // Inline message (not recommended - not localized)
 * {@code @NotBlank(message = "Email is required")}
 * String email;
 * </pre>
 *
 * @see MessageSource
 * @see LocalValidatorFactoryBean
 * @see LocaleInterceptor
 */
@Configuration
@RequiredArgsConstructor
public class ValidationConfig {

    private final MessageSource messageSource;

    /**
     * Configures the validator to use the existing MessageSource for i18n support.
     *
     * <p>This allows validation messages to be localized based on the user's
     * preferred language (resolved by LocaleInterceptor).
     *
     * <p><strong>Features:</strong>
     * <ul>
     *   <li>Automatic locale resolution from LocaleContextHolder</li>
     *   <li>UTF-8 encoding for Turkish characters</li>
     *   <li>Fallback to default locale (Turkish) if message not found</li>
     *   <li>Integration with existing i18n infrastructure</li>
     * </ul>
     *
     * @return configured Validator bean
     */
    @Bean
    public Validator validator() {
        LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();

        // Use the existing MessageSource for validation messages
        // This automatically integrates with:
        // - src/main/resources/i18n/messages.properties
        // - src/main/resources/i18n/messages_tr.properties
        // - src/main/resources/i18n/messages_en.properties
        validatorFactoryBean.setValidationMessageSource(messageSource);

        return validatorFactoryBean;
    }
}