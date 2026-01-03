package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.presentation.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;

@RestController
public abstract class BaseController {

    // Helper method to get current user from JWT
    protected UserPrincipal getCurrentUser(Authentication authentication) {
        return (UserPrincipal) authentication.getPrincipal();
    }

    protected String getCurrentUserId(Authentication authentication) {
        return getCurrentUser(authentication).getUserId();
    }

    protected String getCurrentUserEmail(Authentication authentication) {
        return getCurrentUser(authentication).getEmail();
    }

    protected String getCurrentUserRole(Authentication authentication) {
        return getCurrentUser(authentication).getRole();
    }
}
