package com.iyte_yazilim.proje_pazari.domain.exceptions;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String userId) {
        super("User not found: " + userId);
    }

    /**
     * Use this constructor when the lookup key must not be reflected in the API response to prevent
     * user enumeration attacks (e.g., email-based lookups from public endpoints).
     */
    public UserNotFoundException() {
        super("User not found");
    }
}
