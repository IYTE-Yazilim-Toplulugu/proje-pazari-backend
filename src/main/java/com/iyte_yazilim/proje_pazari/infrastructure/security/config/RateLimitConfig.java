package com.iyte_yazilim.proje_pazari.infrastructure.security.config;

import org.springframework.context.annotation.Configuration;

/** Rate-limit policies keyed to URL path prefixes. */
@Configuration
public class RateLimitConfig {

    public enum Policy {
        LOGIN("/api/v1/auth/login", 5, 60),
        REGISTER("/api/v1/auth/register", 5, 60),
        FORGOT_PASSWORD("/api/v1/auth/forgot-password", 3, 60),
        RESET_PASSWORD("/api/v1/auth/reset-password", 5, 60),
        RESEND_VERIFICATION("/api/v1/auth/resend-verification", 3, 60),
        REFRESH("/api/v1/auth/refresh", 30, 60),
        PROFILE_PICTURE("/api/v1/users/me/profile-picture", 10, 3600);

        public final String pathPrefix;
        public final long limit;
        public final long windowSeconds;

        Policy(String pathPrefix, long limit, long windowSeconds) {
            this.pathPrefix = pathPrefix;
            this.limit = limit;
            this.windowSeconds = windowSeconds;
        }
    }

    public static Policy matchPolicy(String path) {
        for (Policy p : Policy.values()) {
            if (path.startsWith(p.pathPrefix)) {
                return p;
            }
        }
        return null;
    }
}
