package com.iyte_yazilim.proje_pazari.domain.exceptions;

public class VerificationTokenExpiredException extends RuntimeException {
    public VerificationTokenExpiredException(String message) {
        super(message);
    }
}
