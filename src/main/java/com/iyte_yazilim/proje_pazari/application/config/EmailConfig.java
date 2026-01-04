package com.iyte_yazilim.proje_pazari.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

/**
 * Configuration for Thymeleaf email template processing with i18n support.
 *
 * <p>This configuration sets up Thymeleaf to process HTML email templates
 * with support for multiple locales (Turkish and English).
 *
 * <p><strong>Template Structure:</strong>
 * <pre>
 * src/main/resources/templates/emails/
 * ├── tr/                     (Turkish templates)
 * │   ├── welcome.html
 * │   ├── password-reset.html
 * │   └── project-invitation.html
 * └── en/                     (English templates)
 *     ├── welcome.html
 *     ├── password-reset.html
 *     └── project-invitation.html
 * </pre>
 *
 * <p><strong>Features:</strong>
 * <ul>
 *   <li>HTML5 template mode for modern email HTML</li>
 *   <li>UTF-8 encoding for Turkish characters</li>
 *   <li>No caching in development for template changes</li>
 *   <li>Locale-based template resolution</li>
 * </ul>
 *
 * @see SpringTemplateEngine
 */
@Configuration
public class EmailConfig {

    /**
     * Configures the template resolver for email templates.
     *
     * <p>Templates are resolved from the classpath under:
     * {@code templates/emails/{locale}/}
     *
     * <p><strong>Template Resolution Example:</strong>
     * <ul>
     *   <li>Locale: tr, Template: welcome → templates/emails/tr/welcome.html</li>
     *   <li>Locale: en, Template: welcome → templates/emails/en/welcome.html</li>
     * </ul>
     *
     * <p><strong>Configuration Details:</strong>
     * <ul>
     *   <li><strong>Template Mode:</strong> HTML5 for modern HTML email support</li>
     *   <li><strong>Prefix:</strong> /templates/emails/ (base path for all templates)</li>
     *   <li><strong>Suffix:</strong> .html (all templates must end with .html)</li>
     *   <li><strong>Encoding:</strong> UTF-8 (supports Turkish: ç, ğ, ı, ö, ş, ü)</li>
     *   <li><strong>Caching:</strong> Disabled for development (enable in production)</li>
     * </ul>
     *
     * @return configured ITemplateResolver for email templates
     */
    @Bean
    public ITemplateResolver emailTemplateResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();

        // Set template mode to HTML5 for modern email HTML
        templateResolver.setTemplateMode(TemplateMode.HTML);

        // Base path for email templates
        // Templates will be loaded from: classpath:/templates/emails/
        templateResolver.setPrefix("/templates/emails/");

        // All templates must end with .html
        templateResolver.setSuffix(".html");

        // UTF-8 encoding for international characters (Turkish: ç, ğ, ı, ö, ş, ü)
        templateResolver.setCharacterEncoding("UTF-8");

        // Disable caching for development
        // TODO: Enable caching in production for better performance
        // templateResolver.setCacheable(true);
        templateResolver.setCacheable(false);

        // Order of this resolver (lower numbers have higher priority)
        templateResolver.setOrder(1);

        return templateResolver;
    }

    /**
     * Configures the Spring Template Engine for processing email templates.
     *
     * <p>This engine uses the email template resolver to process Thymeleaf
     * templates with locale support.
     *
     * <p><strong>Template Processing Example:</strong>
     * <pre>
     * {@code
     * Context context = new Context(Locale.forLanguageTag("tr"));
     * context.setVariable("userName", "Ahmet");
     * String html = templateEngine.process("tr/welcome", context);
     * }
     * </pre>
     *
     * @return configured SpringTemplateEngine
     */
    @Bean
    public SpringTemplateEngine emailTemplateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();

        // Set the email template resolver
        templateEngine.setTemplateResolver(emailTemplateResolver());

        // Enable Spring EL compiler for better performance
        templateEngine.setEnableSpringELCompiler(true);

        return templateEngine;
    }
}