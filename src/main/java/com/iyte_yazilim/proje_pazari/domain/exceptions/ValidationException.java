package com.iyte_yazilim.proje_pazari.domain.exceptions;

/** Exception thrown when request validation fails. */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
