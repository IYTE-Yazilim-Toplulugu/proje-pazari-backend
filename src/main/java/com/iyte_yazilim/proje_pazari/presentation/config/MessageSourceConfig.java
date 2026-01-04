package com.iyte_yazilim.proje_pazari.presentation.config;

import java.util.Arrays;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

/**
 * Configuration class for internationalization (i18n) support.
 *
 * <p>This configuration provides:
 * <ul>
 *   <li>Message source for localized messages from i18n/messages properties files</li>
 *   <li>Locale resolver based on Accept-Language header</li>
 *   <li>Support for Turkish (tr) and English (en) languages</li>
 * </ul>
 *
 * <p>Message files location: src/main/resources/i18n/
 * <ul>
 *   <li>messages.properties (default - Turkish)</li>
 *   <li>messages_tr.properties (Turkish)</li>
 *   <li>messages_en.properties (English)</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>
 * {@code
 * @Autowired
 * private MessageSource messageSource;
 *
 * String message = messageSource.getMessage("user.not.found", null, locale);
 * }
 * </pre>
 */
@Configuration
public class MessageSourceConfig {

    /**
     * Configures the message source for internationalization.
     *
     * <p>Features:
     * <ul>
     *   <li>Reloadable - can reload messages without restarting (cache: 3600s)</li>
     *   <li>UTF-8 encoding - supports all Unicode characters including Turkish chars</li>
     *   <li>No system locale fallback - uses default locale (tr) if message not found</li>
     * </ul>
     *
     * @return configured MessageSource bean
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource =
                new ReloadableResourceBundleMessageSource();

        // Set base name for message properties files
        messageSource.setBasename("classpath:i18n/messages");

        // Set default encoding to UTF-8 to support Turkish characters (ç, ğ, ı, ö, ş, ü)
        messageSource.setDefaultEncoding("UTF-8");

        // Don't fallback to system locale - use default locale (tr) instead
        messageSource.setFallbackToSystemLocale(false);

        // Cache messages for 1 hour (3600 seconds) for better performance
        // Set to -1 in development for immediate reload, or 0 to disable caching
        messageSource.setCacheSeconds(3600);

        return messageSource;
    }

    /**
     * Configures locale resolver based on Accept-Language HTTP header.
     *
     * <p>Locale resolution priority:
     * <ol>
     *   <li>User's preferred language (from database - handled by UserLocaleInterceptor)</li>
     *   <li>Accept-Language header from HTTP request</li>
     *   <li>Default locale (Turkish - tr)</li>
     * </ol>
     *
     * <p>Supported locales: Turkish (tr), English (en)
     *
     * <p>Example Accept-Language headers:
     * <ul>
     *   <li>Accept-Language: tr → Turkish</li>
     *   <li>Accept-Language: en → English</li>
     *   <li>Accept-Language: en-US → English</li>
     *   <li>Accept-Language: tr-TR → Turkish</li>
     * </ul>
     *
     * @return configured LocaleResolver bean
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();

        // Set default locale to Turkish
        localeResolver.setDefaultLocale(Locale.forLanguageTag("tr"));

        // Define supported locales
        localeResolver.setSupportedLocales(
                Arrays.asList(
                        Locale.forLanguageTag("tr"),  // Turkish
                        Locale.forLanguageTag("en")   // English
                )
        );

        return localeResolver;
    }
}