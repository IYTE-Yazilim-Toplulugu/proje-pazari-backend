package com.iyte_yazilim.proje_pazari.domain.exceptions;

import com.iyte_yazilim.proje_pazari.application.common.ErrorCode;

public class VerificationTokenExpiredException extends DomainException {
    public VerificationTokenExpiredException(String message) {
        super(ErrorCode.VERIFICATION_TOKEN_EXPIRED, message);
    }
}
