package com.iyte_yazilim.proje_pazari.infrastructure.security.config;

import static org.junit.jupiter.api.Assertions.*;

import com.iyte_yazilim.proje_pazari.infrastructure.security.config.RateLimitConfig.Policy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RateLimitConfigTest {

    @Test
    @DisplayName("matchPolicy returns LOGIN for /api/v1/auth/login")
    void matchPolicy_login() {
        assertEquals(Policy.LOGIN, RateLimitConfig.matchPolicy("/api/v1/auth/login"));
    }

    @Test
    @DisplayName("matchPolicy returns REGISTER for /api/v1/auth/register")
    void matchPolicy_register() {
        assertEquals(Policy.REGISTER, RateLimitConfig.matchPolicy("/api/v1/auth/register"));
    }

    @Test
    @DisplayName("matchPolicy returns FORGOT_PASSWORD")
    void matchPolicy_forgotPassword() {
        assertEquals(
                Policy.FORGOT_PASSWORD,
                RateLimitConfig.matchPolicy("/api/v1/auth/forgot-password"));
    }

    @Test
    @DisplayName("matchPolicy returns RESET_PASSWORD")
    void matchPolicy_resetPassword() {
        assertEquals(
                Policy.RESET_PASSWORD, RateLimitConfig.matchPolicy("/api/v1/auth/reset-password"));
    }

    @Test
    @DisplayName("matchPolicy returns RESEND_VERIFICATION")
    void matchPolicy_resendVerification() {
        assertEquals(
                Policy.RESEND_VERIFICATION,
                RateLimitConfig.matchPolicy("/api/v1/auth/resend-verification"));
    }

    @Test
    @DisplayName("matchPolicy returns REFRESH")
    void matchPolicy_refresh() {
        assertEquals(Policy.REFRESH, RateLimitConfig.matchPolicy("/api/v1/auth/refresh"));
    }

    @Test
    @DisplayName("matchPolicy returns PROFILE_PICTURE")
    void matchPolicy_profilePicture() {
        assertEquals(
                Policy.PROFILE_PICTURE,
                RateLimitConfig.matchPolicy("/api/v1/users/me/profile-picture"));
    }

    @Test
    @DisplayName("matchPolicy returns null for unmatched path")
    void matchPolicy_unknown_returnsNull() {
        assertNull(RateLimitConfig.matchPolicy("/api/v1/projects"));
        assertNull(RateLimitConfig.matchPolicy("/health"));
        assertNull(RateLimitConfig.matchPolicy(""));
    }

    @Test
    @DisplayName("Each policy has positive limit and window")
    void policies_havePositiveLimitAndWindow() {
        for (Policy p : Policy.values()) {
            assertTrue(p.limit > 0, p + " limit must be positive");
            assertTrue(p.windowSeconds > 0, p + " windowSeconds must be positive");
        }
    }
}
