package com.iyte_yazilim.proje_pazari.domain.exceptions;

import com.iyte_yazilim.proje_pazari.application.common.ErrorCode;

public class UserNotFoundException extends DomainException {

    public UserNotFoundException(String userId) {
        super(ErrorCode.USER_NOT_FOUND, "User not found: " + userId);
    }

    /**
     * Use this constructor when the lookup key must not be reflected in logs or responses to
     * prevent user enumeration attacks (e.g., email-based lookups from public endpoints).
     */
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND, "User not found");
    }
}
