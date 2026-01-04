package com.iyte_yazilim.proje_pazari.presentation.config;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor for resolving and setting the locale for each HTTP request.
 *
 * <p>This interceptor determines the appropriate locale for the current request
 * based on a priority system, ensuring that localized messages are delivered
 * in the user's preferred language.
 *
 * <p><strong>Locale Resolution Priority:</strong>
 * <ol>
 *   <li><strong>Accept-Language Header</strong> (Highest Priority)
 *       <ul>
 *         <li>Read from HTTP request header</li>
 *         <li>Example: {@code Accept-Language: en} or {@code Accept-Language: tr}</li>
 *         <li>Allows clients to explicitly specify language preference per request</li>
 *       </ul>
 *   </li>
 *   <li><strong>Authenticated User's Preferred Language</strong>
 *       <ul>
 *         <li>Retrieved from user's profile in database</li>
 *         <li>Only checked if user is authenticated</li>
 *         <li>Stored in {@code UserEntity.preferredLanguage} field</li>
 *       </ul>
 *   </li>
 *   <li><strong>Default Locale</strong> (Fallback)
 *       <ul>
 *         <li>Turkish (tr) is the default locale</li>
 *         <li>Used when no other locale information is available</li>
 *       </ul>
 *   </li>
 * </ol>
 *
 * <p><strong>Supported Locales:</strong>
 * <ul>
 *   <li>Turkish (tr) - Default</li>
 *   <li>English (en)</li>
 * </ul>
 *
 * <p><strong>Usage Examples:</strong>
 * <pre>
 * // Client sends Accept-Language header
 * GET /api/v1/users
 * Accept-Language: en
 * // Response messages will be in English
 *
 * // Authenticated user with preferredLanguage = "tr"
 * GET /api/v1/users/me
 * Authorization: Bearer token
 * // Response messages will be in Turkish (if no Accept-Language header)
 *
 * // No Accept-Language and no user preference
 * GET /api/v1/auth/login
 * // Response messages will be in Turkish (default)
 * </pre>
 *
 * <p><strong>Integration:</strong>
 * This interceptor must be registered in {@code WebMvcConfigurer} to be active.
 *
 * @see LocaleContextHolder
 * @see HandlerInterceptor
 * @see UserRepository
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LocaleInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    // Supported locales
    private static final String LOCALE_TR = "tr";
    private static final String LOCALE_EN = "en";
    private static final Locale DEFAULT_LOCALE = Locale.forLanguageTag(LOCALE_TR);

    /**
     * Intercepts incoming requests to resolve and set the appropriate locale.
     *
     * <p>This method is called before the controller handler is executed.
     * It determines the locale based on the priority system and sets it
     * in {@code LocaleContextHolder} for the current request thread.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param handler the handler (controller method) to be executed
     * @return true to continue with the request processing
     */
    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {

        Locale resolvedLocale = resolveLocale(request);

        // Set the resolved locale in LocaleContextHolder
        // This makes it available to MessageService and other components
        LocaleContextHolder.setLocale(resolvedLocale);

        log.debug(
                "Locale resolved for request [{}]: {} (from: {})",
                request.getRequestURI(),
                resolvedLocale.getLanguage(),
                getLocaleSource(request, resolvedLocale)
        );

        return true;
    }

    /**
     * Resolves the locale for the current request based on priority system.
     *
     * <p>Resolution order:
     * <ol>
     *   <li>Accept-Language header (if present and supported)</li>
     *   <li>Authenticated user's preferred language (if authenticated and set)</li>
     *   <li>Default locale (Turkish)</li>
     * </ol>
     *
     * @param request the HTTP request
     * @return the resolved locale
     */
    private Locale resolveLocale(HttpServletRequest request) {
        // Priority 1: Check Accept-Language header
        Locale headerLocale = resolveFromAcceptLanguageHeader(request);
        if (headerLocale != null) {
            return headerLocale;
        }

        // Priority 2: Check authenticated user's preferred language
        Locale userLocale = resolveFromAuthenticatedUser();
        if (userLocale != null) {
            return userLocale;
        }

        // Priority 3: Return default locale
        return DEFAULT_LOCALE;
    }

    /**
     * Resolves locale from Accept-Language HTTP header.
     *
     * <p>Extracts the language from the Accept-Language header and validates
     * it against supported locales (tr, en).
     *
     * @param request the HTTP request
     * @return the resolved locale from header, or null if not present/supported
     */
    private Locale resolveFromAcceptLanguageHeader(HttpServletRequest request) {
        String acceptLanguage = request.getHeader("Accept-Language");

        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            return null;
        }

        // Parse the Accept-Language header
        // Format examples: "en", "en-US", "tr", "tr-TR", "en-US,en;q=0.9,tr;q=0.8"
        String primaryLanguage = extractPrimaryLanguage(acceptLanguage);

        if (isSupported(primaryLanguage)) {
            log.debug("Locale resolved from Accept-Language header: {}", primaryLanguage);
            return Locale.forLanguageTag(primaryLanguage);
        }

        return null;
    }

    /**
     * Resolves locale from authenticated user's preferred language setting.
     *
     * <p>This method checks if the current user is authenticated and has a
     * preferred language set in their profile. If so, that language is used.
     *
     * @return the resolved locale from user preference, or null if not authenticated/not set
     */
    private Locale resolveFromAuthenticatedUser() {
        // Get authentication from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if user is authenticated (not null, not anonymous)
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        try {
            // Get the username (email) from authentication
            String email = authentication.getName();

            if (email == null || email.isBlank()) {
                return null;
            }

            // Fetch user from repository
            UserEntity user = userRepository.findByEmail(email).orElse(null);

            // Check if user has a preferred language set
            if (user != null
                    && user.getPreferredLanguage() != null
                    && !user.getPreferredLanguage().isBlank()) {

                String preferredLang = user.getPreferredLanguage();

                if (isSupported(preferredLang)) {
                    log.debug(
                            "Locale resolved from user preference: {} (user: {})",
                            preferredLang,
                            email
                    );
                    return Locale.forLanguageTag(preferredLang);
                } else {
                    log.warn(
                            "User {} has unsupported preferred language: {}. Using default.",
                            email,
                            preferredLang
                    );
                }
            }
        } catch (Exception e) {
            log.warn("Failed to resolve locale from user preference: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Extracts the primary language from Accept-Language header value.
     *
     * <p>Handles various formats:
     * <ul>
     *   <li>"en" → "en"</li>
     *   <li>"en-US" → "en"</li>
     *   <li>"en-US,en;q=0.9,tr;q=0.8" → "en"</li>
     * </ul>
     *
     * @param acceptLanguage the Accept-Language header value
     * @return the primary language code, or empty string if invalid
     */
    private String extractPrimaryLanguage(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            return "";
        }

        // Split by comma to get first language (highest priority)
        String firstLanguage = acceptLanguage.split(",")[0].trim();

        // Remove quality factor if present (e.g., "en;q=0.9" → "en")
        if (firstLanguage.contains(";")) {
            firstLanguage = firstLanguage.split(";")[0].trim();
        }

        // Extract language code from locale (e.g., "en-US" → "en")
        if (firstLanguage.contains("-")) {
            firstLanguage = firstLanguage.split("-")[0].trim();
        }

        return firstLanguage.toLowerCase();
    }

    /**
     * Checks if the given language code is supported.
     *
     * @param languageCode the language code to check (e.g., "tr", "en")
     * @return true if the language is supported, false otherwise
     */
    private boolean isSupported(String languageCode) {
        if (languageCode == null || languageCode.isBlank()) {
            return false;
        }
        return LOCALE_TR.equalsIgnoreCase(languageCode)
                || LOCALE_EN.equalsIgnoreCase(languageCode);
    }

    /**
     * Gets a human-readable description of the locale source for logging.
     *
     * @param request the HTTP request
     * @param resolvedLocale the resolved locale
     * @return description of the locale source
     */
    private String getLocaleSource(HttpServletRequest request, Locale resolvedLocale) {
        String acceptLanguage = request.getHeader("Accept-Language");
        if (acceptLanguage != null && !acceptLanguage.isBlank()) {
            return "Accept-Language header";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "User preference";
        }

        return "Default locale";
    }

    /**
     * Cleans up after the request is complete.
     *
     * <p>This method is called after the view is rendered. It's a good place
     * to clean up any thread-local resources, though LocaleContextHolder
     * is typically cleaned up automatically by Spring.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param handler the handler that was executed
     * @param ex any exception thrown during request processing
     */
    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            Exception ex) {

        // Clear the locale from LocaleContextHolder to prevent memory leaks
        // This is especially important in thread pool scenarios
        LocaleContextHolder.resetLocaleContext();

        log.trace("LocaleContext cleared for request: {}", request.getRequestURI());
    }
}