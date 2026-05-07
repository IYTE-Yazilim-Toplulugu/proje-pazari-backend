package com.iyte_yazilim.proje_pazari.domain.validators;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.validators.UrlValidator;
import com.iyte_yazilim.proje_pazari.application.validators.ValidUrl;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UrlValidatorTest {

    @Mock private ValidUrl annotation;
    @Mock private ConstraintValidatorContext context;

    private UrlValidator validator;

    @BeforeEach
    void setUp() {
        validator = new UrlValidator();
        when(annotation.allowedDomains()).thenReturn(new String[] {"linkedin.com", "github.com"});
        validator.initialize(annotation);
    }

    @Test
    @DisplayName("Should return true for null value — null handled upstream by @NotBlank")
    void isValid_nullValue_returnsTrue() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    @DisplayName("Should return true for empty string")
    void isValid_emptyString_returnsTrue() {
        assertTrue(validator.isValid("", context));
    }

    @Test
    @DisplayName("Should return true for blank string")
    void isValid_blankString_returnsTrue() {
        assertTrue(validator.isValid("   ", context));
    }

    @Test
    @DisplayName("Should return true for exact domain match")
    void isValid_exactDomainMatch_returnsTrue() {
        assertTrue(validator.isValid("https://linkedin.com/in/user", context));
    }

    @Test
    @DisplayName("Should return true for subdomain that ends with allowed domain")
    void isValid_subdomainMatch_returnsTrue() {
        assertTrue(validator.isValid("https://www.linkedin.com/in/user", context));
    }

    @Test
    @DisplayName("Should return true for second allowed domain")
    void isValid_githubUrl_returnsTrue() {
        assertTrue(validator.isValid("https://github.com/user/repo", context));
    }

    @Test
    @DisplayName("Should return false for a domain not in the allowed list")
    void isValid_notAllowedDomain_returnsFalse() {
        assertFalse(validator.isValid("https://evil.com/trick", context));
    }

    @Test
    @DisplayName("Should return false for a URL that fails URI parsing")
    void isValid_malformedUrl_returnsFalse() {
        assertFalse(validator.isValid("not a url with spaces", context));
    }

    @Test
    @DisplayName("Should return true regardless of scheme when domain matches")
    void isValid_ftpSchemeWithAllowedDomain_returnsTrue() {
        assertTrue(validator.isValid("ftp://linkedin.com/files", context));
    }

    @Test
    @DisplayName("Should return true for any valid URL when no domains are restricted")
    void isValid_emptyAllowedDomains_anyValidUrlIsTrue() {
        UrlValidator openValidator = new UrlValidator();
        ValidUrl openAnnotation = mock(ValidUrl.class);
        when(openAnnotation.allowedDomains()).thenReturn(new String[] {});
        openValidator.initialize(openAnnotation);

        assertTrue(openValidator.isValid("https://any-domain.com/path", context));
    }

    @Test
    @DisplayName("Should return false for URI with null host (e.g. mailto: scheme)")
    void isValid_uriWithNullHost_returnsFalse() {
        assertFalse(validator.isValid("mailto:user@example.com", context));
    }

    @Test
    @DisplayName("Should return false for a partial domain that is not a subdomain")
    void isValid_partialDomainNotSubdomain_returnsFalse() {
        // "notlinkedin.com" is not "linkedin.com" and does not end with ".linkedin.com"
        assertFalse(validator.isValid("https://notlinkedin.com/page", context));
    }
}
