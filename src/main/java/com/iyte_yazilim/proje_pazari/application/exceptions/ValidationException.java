package com.iyte_yazilim.proje_pazari.application.exceptions;

import com.iyte_yazilim.proje_pazari.application.common.ErrorCode;
import com.iyte_yazilim.proje_pazari.domain.exceptions.DomainException;

/** Exception thrown when request validation fails. */
public class ValidationException extends DomainException {

    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
    }

    public ValidationException(String message, Throwable cause) {
        super(ErrorCode.VALIDATION_ERROR, message, cause);
    }
}
