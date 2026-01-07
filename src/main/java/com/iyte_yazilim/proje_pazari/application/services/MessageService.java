package com.iyte_yazilim.proje_pazari.application.services;

import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private final MessageSource messageSource;
    private static final Locale DEFAULT_LOCALE = Locale.forLanguageTag("tr");

    public MessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Get the current locale or default to Turkish if not set
     *
     * @return Current locale or Turkish default
     */
    private Locale getCurrentLocale() {
        Locale locale = LocaleContextHolder.getLocale();
        // If locale is null or is the JVM default (not explicitly set), use Turkish
        if (locale == null || locale.equals(Locale.getDefault())) {
            return DEFAULT_LOCALE;
        }
        return locale;
    }

    /**
     * Get localized message by code
     *
     * @param code Message key from properties file
     * @return Localized message
     */
    public String getMessage(String code) {
        return messageSource.getMessage(code, null, getCurrentLocale());
    }

    /**
     * Get localized message with parameters
     *
     * @param code Message key from properties file
     * @param args Arguments to substitute in the message
     * @return Localized message with substituted parameters
     */
    public String getMessage(String code, Object[] args) {
        return messageSource.getMessage(code, args, getCurrentLocale());
    }

    /**
     * Get localized message with default fallback
     *
     * @param code Message key from properties file
     * @param defaultMessage Default message if key not found
     * @return Localized message or default
     */
    public String getMessage(String code, String defaultMessage) {
        return messageSource.getMessage(code, null, defaultMessage, getCurrentLocale());
    }

    /**
     * Get localized message with parameters and default fallback
     *
     * @param code Message key from properties file
     * @param args Arguments to substitute in the message
     * @param defaultMessage Default message if key not found
     * @return Localized message with substituted parameters or default
     */
    public String getMessage(String code, Object[] args, String defaultMessage) {
        return messageSource.getMessage(code, args, defaultMessage, getCurrentLocale());
    }
}
