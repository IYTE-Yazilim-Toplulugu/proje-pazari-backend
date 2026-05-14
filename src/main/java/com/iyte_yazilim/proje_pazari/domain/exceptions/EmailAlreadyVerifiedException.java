package com.iyte_yazilim.proje_pazari.domain.exceptions;

import com.iyte_yazilim.proje_pazari.application.common.ErrorCode;

public class EmailAlreadyVerifiedException extends DomainException {
    public EmailAlreadyVerifiedException(String message) {
        super(ErrorCode.EMAIL_ALREADY_VERIFIED, message);
    }
}
