package com.iyte_yazilim.proje_pazari.domain.exceptions;

import com.iyte_yazilim.proje_pazari.application.common.ErrorCode;
import lombok.Getter;

/**
 * Base type for all domain-level exceptions.
 *
 * <p>Every {@code DomainException} carries a stable {@link ErrorCode} that {@code
 * GlobalExceptionHandler} translates into the {@code errorCode} field, the numeric {@code code} and
 * the HTTP status of the response — without ever inspecting the exception subtype.
 *
 * <p>The {@code message} passed here is the <b>technical</b> message: it may contain identifiers,
 * email addresses or other internal detail and is intended for server logs only. The user-facing
 * message is resolved separately from {@link ErrorCode#getMessageKey()} and is always localized and
 * safe to display.
 */
@Getter
public class DomainException extends RuntimeException {

    private final ErrorCode errorCode;

    public DomainException(ErrorCode errorCode, String technicalMessage) {
        super(technicalMessage);
        this.errorCode = errorCode;
    }

    public DomainException(ErrorCode errorCode, String technicalMessage, Throwable cause) {
        super(technicalMessage, cause);
        this.errorCode = errorCode;
    }
}
