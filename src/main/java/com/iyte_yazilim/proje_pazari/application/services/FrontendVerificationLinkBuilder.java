package com.iyte_yazilim.proje_pazari.application.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class FrontendVerificationLinkBuilder {

    private static final String DEFAULT_VERIFICATION_PATH = "/verify-email";

    private final String frontendUrl;
    private final String verificationPath;

    public FrontendVerificationLinkBuilder(
            @Value("${app.frontend.url:http://localhost:3000}") String frontendUrl,
            @Value("${app.frontend.verify-email-path:/verify-email}") String verificationPath) {
        this.frontendUrl = frontendUrl;
        this.verificationPath = normalizePath(verificationPath);
    }

    public String build(String token) {
        return UriComponentsBuilder.fromUriString(frontendUrl)
                .replacePath(verificationPath)
                .replaceQuery(null)
                .fragment(null)
                .queryParam("token", "{token}")
                .encode()
                .buildAndExpand(token)
                .toUriString();
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return DEFAULT_VERIFICATION_PATH;
        }

        String trimmedPath = path.trim();
        return trimmedPath.startsWith("/") ? trimmedPath : "/" + trimmedPath;
    }
}
