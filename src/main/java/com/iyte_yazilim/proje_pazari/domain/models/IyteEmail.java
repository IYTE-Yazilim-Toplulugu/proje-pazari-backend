package com.iyte_yazilim.proje_pazari.domain.models;

import java.util.List;
import java.util.Objects;

/**
 * Value Object for IYTE email addresses.
 *
 * <p>Enforces that email addresses must be from allowed IYTE domains. This is an immutable value
 * object that encapsulates email validation logic within the domain layer.
 */
public final class IyteEmail {

    private static final List<String> ALLOWED_DOMAINS = List.of("@std.iyte.edu.tr", "@iyte.edu.tr");

    private final String value;

    private IyteEmail(String value) {
        this.value = value;
    }

    /**
     * Factory method to create an IyteEmail instance.
     *
     * @param email the email address to validate and wrap
     * @return IyteEmail instance
     * @throws IllegalArgumentException if email is invalid or not from an IYTE domain
     */
    public static IyteEmail of(String email) {
        validate(email);
        return new IyteEmail(email.trim());
    }

    private static void validate(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        String normalizedEmail = email.trim().toLowerCase();

        boolean isValid =
                ALLOWED_DOMAINS.stream()
                        .anyMatch(domain -> normalizedEmail.endsWith(domain.toLowerCase()));

        if (!isValid) {
            throw new IllegalArgumentException(
                    "Email must be from @std.iyte.edu.tr or @iyte.edu.tr domain");
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IyteEmail iyteEmail = (IyteEmail) o;
        return Objects.equals(value.toLowerCase(), iyteEmail.value.toLowerCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(value.toLowerCase());
    }

    @Override
    public String toString() {
        return value;
    }
}
