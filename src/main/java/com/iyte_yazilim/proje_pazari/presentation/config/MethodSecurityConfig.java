package com.iyte_yazilim.proje_pazari.presentation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity(
    prePostEnabled = true, // Enables @PreAuthorize and @PostAuthorize
    securedEnabled = true, // Enables @Secured
    jsr250Enabled = true   // Enables @RolesAllowed
)
public class MethodSecurityConfig {

    /**
     * Optional: Define a custom expression handler if you plan to implement 
     * custom permission evaluators (e.g., @PreAuthorize("hasPermission(...)"))
     */
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        // You can set a custom PermissionEvaluator here if needed
        return expressionHandler;
    }
}