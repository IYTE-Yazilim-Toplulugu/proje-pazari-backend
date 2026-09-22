package com.iyte_yazilim.proje_pazari.domain.exceptions;

import com.iyte_yazilim.proje_pazari.application.common.ErrorCode;

public class EmailSendException extends DomainException {
    public EmailSendException(String message, Throwable cause) {
        super(ErrorCode.EMAIL_SEND_FAILED, message, cause);
    }
}
