package com.iyte_yazilim.proje_pazari.domain.exceptions;

import com.iyte_yazilim.proje_pazari.application.common.ErrorCode;

public class EmailNotVerifiedException extends DomainException {
    public EmailNotVerifiedException(String message) {
        super(ErrorCode.EMAIL_NOT_VERIFIED, message);
    }
}
