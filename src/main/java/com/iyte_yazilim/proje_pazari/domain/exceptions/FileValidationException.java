package com.iyte_yazilim.proje_pazari.domain.exceptions;

import com.iyte_yazilim.proje_pazari.application.common.ErrorCode;

public class FileValidationException extends DomainException {

    public FileValidationException(String message) {
        super(ErrorCode.FILE_VALIDATION_FAILED, message);
    }

    public FileValidationException(String message, Throwable cause) {
        super(ErrorCode.FILE_VALIDATION_FAILED, message, cause);
    }
}
