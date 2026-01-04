package com.iyte_yazilim.proje_pazari.application.services;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service for centralized access to internationalized messages.
 *
 * <p>This service provides a clean API for retrieving localized messages from
 * message property files (messages.properties, messages_en.properties, messages_tr.properties).
 *
 * <p>Features:
 * <ul>
 *   <li>Automatic locale resolution from LocaleContextHolder</li>
 *   <li>Support for parameterized messages</li>
 *   <li>Fallback to message code if translation not found</li>
 *   <li>Convenience methods for success and error messages</li>
 * </ul>
 *
 * <p>Usage examples:
 * <pre>
 * {@code
 * // Simple message
 * String msg = messageService.getMessage("user.not.found");
 *
 * // Message with parameters
 * String msg = messageService.getMessage("user.not.found.with.id", new Object[]{"123"});
 *
 * // Message with explicit locale
 * String msg = messageService.getMessage("user.not.found", null, Locale.ENGLISH);
 *
 * // Success message
 * String msg = messageService.getSuccessMessage("user.registered.success");
 *
 * // Error message
 * String msg = messageService.getErrorMessage("error.unauthorized");
 * }
 * </pre>
 *
 * @see MessageSource
 * @see LocaleContextHolder
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageSource messageSource;

    /**
     * Retrieves a localized message using the current locale from LocaleContextHolder.
     *
     * <p>The locale is automatically determined by:
     * <ol>
     *   <li>User's preferred language (from database via UserLocaleInterceptor)</li>
     *   <li>Accept-Language HTTP header</li>
     *   <li>Default locale (Turkish - tr)</li>
     * </ol>
     *
     * @param code the message code/key (e.g., "user.not.found")
     * @return the localized message, or the code itself if message not found
     *
     * @example
     * <pre>
     * String message = messageService.getMessage("user.not.found");
     * // Returns: "Kullanıcı bulunamadı" (tr) or "User not found" (en)
     * </pre>
     */
    public String getMessage(String code) {
        return getMessage(code, null, LocaleContextHolder.getLocale());
    }

    /**
     * Retrieves a localized message with parameters using the current locale.
     *
     * <p>Parameters are substituted into the message using {0}, {1}, etc. placeholders.
     *
     * @param code the message code/key
     * @param args the arguments to substitute into the message (can be null)
     * @return the localized message with substituted parameters, or the code itself if not found
     *
     * @example
     * <pre>
     * String message = messageService.getMessage("user.not.found.with.id", new Object[]{"123"});
     * // Returns: "ID 123 ile kullanıcı bulunamadı" (tr) or "User with ID 123 not found" (en)
     * </pre>
     */
    public String getMessage(String code, Object[] args) {
        return getMessage(code, args, LocaleContextHolder.getLocale());
    }

    /**
     * Retrieves a localized message with parameters using the specified locale.
     *
     * <p>This method provides full control over locale selection.
     * If the message is not found, the code itself is returned as a fallback.
     *
     * @param code the message code/key
     * @param args the arguments to substitute into the message (can be null)
     * @param locale the locale to use for message retrieval
     * @return the localized message with substituted parameters, or the code itself if not found
     *
     * @example
     * <pre>
     * String message = messageService.getMessage("user.not.found", null, Locale.ENGLISH);
     * // Returns: "User not found" (always in English)
     * </pre>
     */
    public String getMessage(String code, Object[] args, Locale locale) {
        try {
            return messageSource.getMessage(code, args, locale);
        } catch (NoSuchMessageException e) {
            log.warn(
                    "Message not found for code '{}' and locale '{}'. Returning code as fallback.",
                    code,
                    locale);
            return code;
        }
    }

    /**
     * Retrieves a localized message with a default fallback value.
     *
     * <p>If the message is not found, the provided default message is returned
     * instead of the message code.
     *
     * @param code the message code/key
     * @param defaultMessage the default message to return if code not found
     * @return the localized message, or the default message if not found
     *
     * @example
     * <pre>
     * String message = messageService.getMessage("custom.message", "Operation successful");
     * // Returns the localized message, or "Operation successful" if not found
     * </pre>
     */
    public String getMessage(String code, String defaultMessage) {
        return getMessage(code, null, defaultMessage, LocaleContextHolder.getLocale());
    }

    /**
     * Retrieves a localized message with parameters and a default fallback value.
     *
     * @param code the message code/key
     * @param args the arguments to substitute into the message
     * @param defaultMessage the default message to return if code not found
     * @return the localized message with substituted parameters, or the default message
     *
     * @example
     * <pre>
     * String message = messageService.getMessage(
     *     "custom.greeting",
     *     new Object[]{"John"},
     *     "Hello"
     * );
     * </pre>
     */
    public String getMessage(String code, Object[] args, String defaultMessage) {
        return getMessage(code, args, defaultMessage, LocaleContextHolder.getLocale());
    }

    /**
     * Retrieves a localized message with parameters, default fallback, and specified locale.
     *
     * <p>This is the most flexible method, providing full control over all parameters.
     *
     * @param code the message code/key
     * @param args the arguments to substitute into the message
     * @param defaultMessage the default message to return if code not found
     * @param locale the locale to use for message retrieval
     * @return the localized message with substituted parameters, or the default message
     */
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        try {
            return messageSource.getMessage(code, args, defaultMessage, locale);
        } catch (Exception e) {
            log.warn(
                    "Error retrieving message for code '{}' and locale '{}'. Returning default message.",
                    code,
                    locale);
            return defaultMessage != null ? defaultMessage : code;
        }
    }

    /**
     * Convenience method for retrieving success messages.
     *
     * <p>Typically used for messages with "success" suffix (e.g., "user.registered.success").
     *
     * @param operation the operation message code (e.g., "user.registered.success")
     * @return the localized success message
     *
     * @example
     * <pre>
     * String message = messageService.getSuccessMessage("user.registered.success");
     * // Returns: "Kullanıcı başarıyla kaydedildi" (tr) or "User registered successfully" (en)
     * </pre>
     */
    public String getSuccessMessage(String operation) {
        return getMessage(operation);
    }

    /**
     * Convenience method for retrieving success messages with parameters.
     *
     * @param operation the operation message code
     * @param args the arguments to substitute into the message
     * @return the localized success message with substituted parameters
     *
     * @example
     * <pre>
     * String message = messageService.getSuccessMessage(
     *     "project.created.with.name",
     *     new Object[]{"My Project"}
     * );
     * </pre>
     */
    public String getSuccessMessage(String operation, Object[] args) {
        return getMessage(operation, args);
    }

    /**
     * Convenience method for retrieving error messages.
     *
     * <p>Typically used for messages with "error." prefix (e.g., "error.unauthorized").
     *
     * @param error the error message code (e.g., "error.unauthorized")
     * @return the localized error message
     *
     * @example
     * <pre>
     * String message = messageService.getErrorMessage("error.unauthorized");
     * // Returns: "Yetkisiz erişim" (tr) or "Unauthorized access" (en)
     * </pre>
     */
    public String getErrorMessage(String error) {
        return getMessage(error);
    }

    /**
     * Convenience method for retrieving error messages with parameters.
     *
     * @param error the error message code
     * @param args the arguments to substitute into the message
     * @return the localized error message with substituted parameters
     *
     * @example
     * <pre>
     * String message = messageService.getErrorMessage(
     *     "error.file.upload",
     *     new Object[]{"Invalid format"}
     * );
     * // Returns: "Dosya yükleme başarısız: Invalid format" (tr)
     * </pre>
     */
    public String getErrorMessage(String error, Object[] args) {
        return getMessage(error, args);
    }

    /**
     * Retrieves the current locale from LocaleContextHolder.
     *
     * <p>Useful for debugging or when you need to know which locale is being used.
     *
     * @return the current locale
     */
    public Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    /**
     * Checks if a message exists for the given code and current locale.
     *
     * @param code the message code to check
     * @return true if the message exists, false otherwise
     *
     * @example
     * <pre>
     * if (messageService.hasMessage("custom.message")) {
     *     String msg = messageService.getMessage("custom.message");
     * }
     * </pre>
     */
    public boolean hasMessage(String code) {
        return hasMessage(code, LocaleContextHolder.getLocale());
    }

    /**
     * Checks if a message exists for the given code and specified locale.
     *
     * @param code the message code to check
     * @param locale the locale to check against
     * @return true if the message exists, false otherwise
     */
    public boolean hasMessage(String code, Locale locale) {
        try {
            messageSource.getMessage(code, null, locale);
            return true;
        } catch (NoSuchMessageException e) {
            return false;
        }
    }
}