package com.iyte_yazilim.proje_pazari.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@SuppressWarnings("unused")
public enum ResponseCode {
    // Success
    SUCCESS(0),
    NO_CONTENT(1),
    CREATED(2),
    ACCEPTED(3),

    // Client Errors
    BAD_REQUEST(4),
    UNAUTHORIZED(5),
    FORBIDDEN(6),
    NOT_FOUND(7),
    CONFLICT(8),
    VALIDATION_ERROR(9),

    // Server Errors
    INTERNAL_SERVER_ERROR(10);

    private final int status;
}
