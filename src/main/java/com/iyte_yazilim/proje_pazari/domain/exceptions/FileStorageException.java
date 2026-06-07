package com.iyte_yazilim.proje_pazari.domain.exceptions;

import com.iyte_yazilim.proje_pazari.application.common.ErrorCode;

public class FileStorageException extends DomainException {

    public FileStorageException(String message) {
        super(ErrorCode.FILE_STORAGE_ERROR, message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(ErrorCode.FILE_STORAGE_ERROR, message, cause);
    }
}
