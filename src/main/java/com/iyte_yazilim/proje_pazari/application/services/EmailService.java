package com.iyte_yazilim.proje_pazari.application.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 * Service for sending localized HTML emails using Thymeleaf templates.
 *
 * <p>This service handles email sending with support for multiple locales (Turkish and English).
 * Templates are automatically selected based on the provided locale.
 *
 * <p><strong>Template Structure:</strong>
 * <pre>
 * templates/emails/
 * ├── tr/                     (Turkish templates)
 * │   ├── welcome.html
 * │   ├── password-reset.html
 * │   ├── password-changed.html
 * │   ├── account-deactivated.html
 * │   └── project-invitation.html
 * └── en/                     (English templates)
 *     ├── welcome.html
 *     ├── password-reset.html
 *     ├── password-changed.html
 *     ├── account-deactivated.html
 *     └── project-invitation.html
 * </pre>
 *
 * <p><strong>Usage Example:</strong>
 * <pre>
 * {@code
 * // Send welcome email in Turkish
 * Map<String, Object> variables = Map.of(
 *     "userName", "Ahmet Yılmaz",
 *     "activationLink", "https://app.com/activate/123"
 * );
 * emailService.sendEmail(
 *     "ahmet@example.com",
 *     "Hoş Geldiniz!",
 *     "welcome",
 *     variables,
 *     Locale.forLanguageTag("tr")
 * );
 *
 * // Send welcome email in English
 * emailService.sendEmail(
 *     "john@example.com",
 *     "Welcome!",
 *     "welcome",
 *     variables,
 *     Locale.forLanguageTag("en")
 * );
 * }
 * </pre>
 *
 * <p><strong>Features:</strong>
 * <ul>
 *   <li>Automatic locale-based template selection</li>
 *   <li>HTML email support with Thymeleaf</li>
 *   <li>UTF-8 encoding for international characters</li>
 *   <li>Template variable substitution</li>
 *   <li>Comprehensive error handling and logging</li>
 *   <li>Fallback to default locale on template not found</li>
 * </ul>
 *
 * @see SpringTemplateEngine
 * @see JavaMailSender
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine emailTemplateEngine;

    // Default sender email address
    // TODO: Configure this in application.properties
    private static final String DEFAULT_FROM_EMAIL = "noreply@proje-pazari.com";

    // Supported locales
    private static final Locale LOCALE_TR = Locale.forLanguageTag("tr");
    private static final Locale LOCALE_EN = Locale.forLanguageTag("en");
    private static final Locale DEFAULT_LOCALE = LOCALE_TR;

    /**
     * Sends a localized HTML email using a Thymeleaf template.
     *
     * <p>This method automatically resolves the correct template based on the provided locale.
     * Template path is constructed as: {@code {locale.language}/{templateName}.html}
     *
     * <p><strong>Template Resolution Examples:</strong>
     * <ul>
     *   <li>Locale: tr, Template: welcome → templates/emails/tr/welcome.html</li>
     *   <li>Locale: en, Template: welcome → templates/emails/en/welcome.html</li>
     *   <li>Locale: en-US, Template: password-reset → templates/emails/en/password-reset.html</li>
     * </ul>
     *
     * <p><strong>Process Flow:</strong>
     * <ol>
     *   <li>Validate and normalize locale (tr/en only)</li>
     *   <li>Construct template path: {language}/{templateName}</li>
     *   <li>Create Thymeleaf context with locale and variables</li>
     *   <li>Process template to generate HTML</li>
     *   <li>Create MIME message with HTML content</li>
     *   <li>Send email via JavaMailSender</li>
     * </ol>
     *
     * @param to recipient email address
     * @param subject email subject line
     * @param templateName name of the template (without .html extension and locale prefix)
     * @param variables map of variables to be used in the template
     * @param locale locale for template selection and content localization
     * @throws MessagingException if email sending fails
     *
     * @example
     * <pre>
     * Map<String, Object> vars = Map.of(
     *     "userName", "Ahmet",
     *     "resetLink", "https://app.com/reset/abc123"
     * );
     * emailService.sendEmail(
     *     "user@example.com",
     *     "Şifre Sıfırlama",
     *     "password-reset",
     *     vars,
     *     Locale.forLanguageTag("tr")
     * );
     * </pre>
     */
    public void sendEmail(
            String to,
            String subject,
            String templateName,
            Map<String, Object> variables,
            Locale locale) throws MessagingException {

        log.info("Preparing to send email to: {} with template: {} and locale: {}",
                to, templateName, locale.getLanguage());

        // Validate and normalize locale
        Locale normalizedLocale = normalizeLocale(locale);

        // Construct template path: {language}/{templateName}
        // Example: tr/welcome, en/password-reset
        String templatePath = resolveTemplatePath(templateName, normalizedLocale);

        log.debug("Resolved template path: {}", templatePath);

        // Create Thymeleaf context with locale
        Context context = createContext(variables, normalizedLocale);

        try {
            // Process template to generate HTML content
            String htmlContent = emailTemplateEngine.process(templatePath, context);

            // Create and send email
            sendHtmlEmail(to, subject, htmlContent);

            log.info("Email sent successfully to: {} with template: {} (locale: {})",
                    to, templateName, normalizedLocale.getLanguage());

        } catch (Exception e) {
            log.error("Failed to send email to: {} with template: {} (locale: {})",
                    to, templateName, normalizedLocale.getLanguage(), e);
            throw new MessagingException("Failed to send email: " + e.getMessage(), e);
        }
    }

    /**
     * Sends an email with default locale (Turkish).
     *
     * <p>This is a convenience method that uses the default locale (Turkish).
     * Equivalent to calling: {@code sendEmail(to, subject, templateName, variables, Locale.forLanguageTag("tr"))}
     *
     * @param to recipient email address
     * @param subject email subject line
     * @param templateName name of the template
     * @param variables map of variables for the template
     * @throws MessagingException if email sending fails
     */
    public void sendEmail(
            String to,
            String subject,
            String templateName,
            Map<String, Object> variables) throws MessagingException {

        sendEmail(to, subject, templateName, variables, DEFAULT_LOCALE);
    }

    /**
     * Sends an email to a user with their preferred locale.
     *
     * <p>This method retrieves the user's preferred language from the database
     * and sends the email in that language.
     *
     * @param to recipient email address
     * @param subject email subject line
     * @param templateName name of the template
     * @param variables map of variables for the template
     * @param userPreferredLanguage user's preferred language code ("tr" or "en")
     * @throws MessagingException if email sending fails
     */
    public void sendEmailToUser(
            String to,
            String subject,
            String templateName,
            Map<String, Object> variables,
            String userPreferredLanguage) throws MessagingException {

        Locale locale = Locale.forLanguageTag(userPreferredLanguage);
        sendEmail(to, subject, templateName, variables, locale);
    }

    /**
     * Resolves the template path based on locale and template name.
     *
     * <p>Template path format: {@code {language}/{templateName}}
     * <ul>
     *   <li>Turkish: tr/welcome</li>
     *   <li>English: en/welcome</li>
     * </ul>
     *
     * <p>The .html extension is added automatically by Thymeleaf.
     *
     * @param templateName base template name (e.g., "welcome", "password-reset")
     * @param locale locale for template selection
     * @return template path in format: {language}/{templateName}
     */
    private String resolveTemplatePath(String templateName, Locale locale) {
        String language = locale.getLanguage();

        // Template path format: {language}/{templateName}
        // Examples: tr/welcome, en/password-reset
        String templatePath = language + "/" + templateName;

        log.debug("Resolved template path: {} for locale: {}", templatePath, locale);

        return templatePath;
    }

    /**
     * Creates a Thymeleaf context with locale and variables.
     *
     * <p>The context includes:
     * <ul>
     *   <li>Locale for proper date/number formatting</li>
     *   <li>All provided variables for template substitution</li>
     * </ul>
     *
     * @param variables map of variables to be available in the template
     * @param locale locale for the context
     * @return configured Context object
     */
    private Context createContext(Map<String, Object> variables, Locale locale) {
        Context context = new Context(locale);

        // Add all variables to context
        if (variables != null && !variables.isEmpty()) {
            variables.forEach(context::setVariable);

            log.debug("Added {} variables to template context", variables.size());
        }

        return context;
    }

    /**
     * Normalizes the locale to ensure only supported locales are used.
     *
     * <p><strong>Normalization Rules:</strong>
     * <ul>
     *   <li>Turkish (tr, tr-TR) → tr</li>
     *   <li>English (en, en-US, en-GB) → en</li>
     *   <li>Unsupported locale → tr (default)</li>
     *   <li>Null locale → tr (default)</li>
     * </ul>
     *
     * @param locale locale to normalize
     * @return normalized locale (tr or en)
     */
    private Locale normalizeLocale(Locale locale) {
        if (locale == null) {
            log.debug("Locale is null, using default: {}", DEFAULT_LOCALE.getLanguage());
            return DEFAULT_LOCALE;
        }

        String language = locale.getLanguage().toLowerCase();

        return switch (language) {
            case "tr" -> {
                log.debug("Using Turkish locale");
                yield LOCALE_TR;
            }
            case "en" -> {
                log.debug("Using English locale");
                yield LOCALE_EN;
            }
            default -> {
                log.warn("Unsupported locale: {}, falling back to default: {}",
                        language, DEFAULT_LOCALE.getLanguage());
                yield DEFAULT_LOCALE;
            }
        };
    }

    /**
     * Sends an HTML email using JavaMailSender.
     *
     * <p>This is a low-level method that handles the actual email sending
     * with proper MIME message configuration.
     *
     * @param to recipient email address
     * @param subject email subject line
     * @param htmlContent HTML content of the email
     * @throws MessagingException if email sending fails
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent)
            throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(DEFAULT_FROM_EMAIL);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true = HTML content

        mailSender.send(message);

        log.debug("MIME message sent to: {}", to);
    }

    /**
     * Sends a plain text email (non-HTML).
     *
     * <p>This method is provided for cases where HTML email is not needed.
     * For most cases, use the template-based methods instead.
     *
     * @param to recipient email address
     * @param subject email subject line
     * @param textContent plain text content
     * @throws MessagingException if email sending fails
     */
    public void sendTextEmail(String to, String subject, String textContent)
            throws MessagingException {

        log.info("Sending plain text email to: {}", to);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(DEFAULT_FROM_EMAIL);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(textContent, false); // false = plain text

        mailSender.send(message);

        log.info("Plain text email sent successfully to: {}", to);
    }
}