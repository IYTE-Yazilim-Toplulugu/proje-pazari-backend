package com.iyte_yazilim.proje_pazari.application.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Enumeration of response codes used in API responses.
 *
 * <p>These codes provide a consistent way to communicate the result of API operations, separate
 * from HTTP status codes.
 *
 * <h2>Code Categories:</h2>
 *
 * <ul>
 *   <li><b>0-3:</b> General success codes
 *   <li><b>4-9:</b> Client error codes
 *   <li><b>10:</b> Server error code
 *   <li><b>11+:</b> Application flow success states
 * </ul>
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2024-01-01
 * @see com.iyte_yazilim.proje_pazari.application.common.ApiResponse
 */
@Getter
@AllArgsConstructor
@SuppressWarnings("unused")
public enum ResponseCode {
    // Success codes
    /** Operation completed successfully. */
    SUCCESS(0),

    /** Operation completed with no content to return. */
    NO_CONTENT(1),

    /** Resource was created successfully. */
    CREATED(2),

    /** Request was accepted for processing. */
    ACCEPTED(3),

    /** User registered successfully and must verify their email address. */
    REGISTERED_NEEDS_VERIFICATION(11),

    // Client error codes
    /** Request was malformed or invalid. */
    BAD_REQUEST(4),

    /** Authentication required or failed. */
    UNAUTHORIZED(5),

    /** User lacks permission for this operation. */
    FORBIDDEN(6),

    /** Requested resource was not found. */
    NOT_FOUND(7),

    /** Request conflicts with current state. */
    CONFLICT(8),

    /** Request failed validation checks. */
    VALIDATION_ERROR(9),

    // Server error codes
    /** Unexpected server error occurred. */
    INTERNAL_SERVER_ERROR(10);

    /** Numeric status code for this response type. */
    @JsonValue private final int status;

    @JsonCreator
    public static ResponseCode fromIntValue(int value) {
        for (ResponseCode responseCode : ResponseCode.values()) {
            if (responseCode.getStatus() == value) {
                return responseCode;
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * Maps this response code to the HTTP status it should be served with. Keeping the mapping here
     * lets callers (e.g. {@code GlobalExceptionHandler}) derive the status in one place instead of
     * hardcoding it per exception handler.
     *
     * @return the corresponding {@link HttpStatus}
     */
    public HttpStatus httpStatus() {
        return switch (this) {
            case SUCCESS -> HttpStatus.OK;
            case NO_CONTENT -> HttpStatus.NO_CONTENT;
            case CREATED, REGISTERED_NEEDS_VERIFICATION -> HttpStatus.CREATED;
            case ACCEPTED -> HttpStatus.ACCEPTED;
            case BAD_REQUEST, VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
