package com.iyte_yazilim.proje_pazari.presentation.config;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor to set user's preferred language for localized responses.
 * Priority: 1. User's preferred language (from DB) 2. Accept-Language header 3. Default (tr)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserLocaleInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // If user is authenticated, check their language preference
        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {

            String email = authentication.getName();

            try {
                UserEntity user = userRepository.findByEmail(email).orElse(null);

                if (user != null
                        && user.getPreferredLanguage() != null
                        && !user.getPreferredLanguage().isBlank()) {
                    // User has a language preference - use it
                    Locale userLocale = Locale.forLanguageTag(user.getPreferredLanguage());
                    LocaleContextHolder.setLocale(userLocale);
                    log.debug(
                            "Set locale to user preference: {}",
                            user.getPreferredLanguage());
                    return true;
                }
            } catch (Exception e) {
                log.warn("Failed to load user language preference for: {}", email, e);
            }
        }

        // If no user preference, AcceptHeaderLocaleResolver will handle Accept-Language header
        // (configured in InternationalizationConfig)

        return true;
    }
}