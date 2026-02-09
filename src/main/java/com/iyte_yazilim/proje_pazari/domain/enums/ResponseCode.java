package com.iyte_yazilim.proje_pazari.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration of response codes used in API responses.
 *
 * <p>These codes provide a consistent way to communicate the result of API operations, separate
 * from HTTP status codes.
 *
 * <h2>Code Categories:</h2>
 *
 * <ul>
 *   <li><b>0-3:</b> Success codes
 *   <li><b>4-9:</b> Client error codes
 *   <li><b>10+:</b> Server error codes
 * </ul>
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2024-01-01
 * @see com.iyte_yazilim.proje_pazari.domain.models.ApiResponse
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
}
