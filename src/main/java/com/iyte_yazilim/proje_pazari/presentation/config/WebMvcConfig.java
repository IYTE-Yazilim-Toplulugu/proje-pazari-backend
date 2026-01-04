package com.iyte_yazilim.proje_pazari.presentation.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for Spring MVC customizations.
 *
 * <p>This configuration registers custom interceptors and configures
 * web-related components for the application.
 *
 * <p>Current configurations:
 * <ul>
 *   <li>Locale resolution interceptor for internationalization</li>
 * </ul>
 *
 * @see LocaleInterceptor
 * @see WebMvcConfigurer
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LocaleInterceptor localeInterceptor;

    /**
     * Registers interceptors with the Spring MVC interceptor registry.
     *
     * <p>Interceptors are executed in the order they are registered.
     * Each interceptor can inspect and modify the request/response before
     * and after the controller handler is executed.
     *
     * <p><strong>Current Interceptors:</strong>
     * <ol>
     *   <li>{@link LocaleInterceptor} - Resolves and sets the locale for each request</li>
     * </ol>
     *
     * <p><strong>Interceptor Configuration:</strong>
     * <ul>
     *   <li><strong>Included paths:</strong> All API endpoints ({@code /api/**})</li>
     *   <li><strong>Excluded paths:</strong>
     *       <ul>
     *         <li>Swagger UI: {@code /swagger-ui/**}, {@code /swagger-ui.html}</li>
     *         <li>OpenAPI docs: {@code /v3/api-docs/**}</li>
     *         <li>Actuator endpoints: {@code /actuator/**}</li>
     *       </ul>
     *   </li>
     * </ul>
     *
     * <p><strong>Why exclude certain paths?</strong>
     * <ul>
     *   <li>Swagger/OpenAPI: Documentation UI doesn't need internationalization</li>
     *   <li>Actuator: Health checks and metrics are typically not localized</li>
     *   <li>Performance: Reduces unnecessary processing for non-API endpoints</li>
     * </ul>
     *
     * @param registry the interceptor registry to add interceptors to
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeInterceptor)
                // Apply interceptor to all API endpoints
                .addPathPatterns("/api/**")
                // Exclude documentation and monitoring endpoints
                .excludePathPatterns(
                        "/swagger-ui/**",           // Swagger UI static resources
                        "/v3/api-docs/**",          // OpenAPI documentation endpoints
                        "/swagger-ui.html",         // Swagger UI main page
                        "/actuator/**"              // Spring Boot Actuator endpoints
                );
    }

    /**
     * Additional configuration methods can be added here as needed:
     *
     * <pre>
     * {@code
     * @Override
     * public void addCorsMappings(CorsRegistry registry) {
     *     // CORS configuration
     * }
     *
     * @Override
     * public void addResourceHandlers(ResourceHandlerRegistry registry) {
     *     // Static resource handlers
     * }
     *
     * @Override
     * public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
     *     // Custom message converters
     * }
     * }
     * </pre>
     */
}