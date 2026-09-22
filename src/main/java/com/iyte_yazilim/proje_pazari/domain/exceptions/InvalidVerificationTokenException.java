package com.iyte_yazilim.proje_pazari.domain.exceptions;

import com.iyte_yazilim.proje_pazari.application.common.ErrorCode;

public class InvalidVerificationTokenException extends DomainException {
    public InvalidVerificationTokenException(String message) {
        super(ErrorCode.INVALID_VERIFICATION_TOKEN, message);
    }
}
