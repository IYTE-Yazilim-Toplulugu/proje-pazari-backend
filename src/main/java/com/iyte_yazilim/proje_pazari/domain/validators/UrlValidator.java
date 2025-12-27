package com.iyte_yazilim.proje_pazari.domain.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.URI;
import java.util.Arrays;

public class UrlValidator implements ConstraintValidator<ValidUrl, String> {

    private String[] allowedDomains;

    @Override
    public void initialize(ValidUrl constraintAnnotation) {
        this.allowedDomains = constraintAnnotation.allowedDomains();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // null/blank values are handled by @NotBlank
        }

        try {
            URI uri = new URI(value);
            String host = uri.getHost();

            if (host == null) {
                return false;
            }

            if (allowedDomains.length == 0) {
                return true; // No domain restriction
            }

            // Check if host matches any allowed domain
            return Arrays.stream(allowedDomains)
                    .anyMatch(domain -> host.equals(domain) || host.endsWith("." + domain));
        } catch (java.net.URISyntaxException e) {
            return false;
        }
    }
}
