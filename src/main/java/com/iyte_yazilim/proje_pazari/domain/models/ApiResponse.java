package com.iyte_yazilim.proje_pazari.domain.models;

import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * ApiResponse
 *
 * Represents a standardized HTTP response body.
 * Adheres to Clean Architecture by decoupling internal logic from external JSON
 * structure.
 *
 * @param <T> the type of data contained in the response
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // 1. Don't send "data": null to the client on errors
@SuppressWarnings("unused")
public class ApiResponse<T> {

    private final T data;
    private final String message;
    private final ResponseCode code; // 2. Using the Enum strictly
    private final LocalDateTime timestamp; // 3. Audit trail

    // Private constructor enforces usage of static factory methods
    private ApiResponse(T data, String message, ResponseCode code) {
        this.data = data;
        this.message = message;
        this.code = code;
        this.timestamp = LocalDateTime.now();
    }

    // --- SUCCESS RESPONSES ---

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(data, message, ResponseCode.SUCCESS);
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return new ApiResponse<>(data, message, ResponseCode.CREATED);
    }

    public static <T> ApiResponse<T> accepted(T data, String message) {
        return new ApiResponse<>(data, message, ResponseCode.ACCEPTED);
    }

    // --- ERROR RESPONSES ---

    public static <T> ApiResponse<T> badRequest(String message) {
        // Fix: Use Enum, do not pass raw '400' int
        return new ApiResponse<>(null, message, ResponseCode.BAD_REQUEST);
    }

    public static <T> ApiResponse<T> internalError(String message) {
        return new ApiResponse<>(null, message, ResponseCode.INTERNAL_SERVER_ERROR);
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(null, message, ResponseCode.NOT_FOUND);
    }

    public static <T> ApiResponse<T> unauthorized(String message) {
        return new ApiResponse<>(null, message, ResponseCode.UNAUTHORIZED);
    }

    public static <T> ApiResponse<T> forbidden(String message) {
        return new ApiResponse<>(null, message, ResponseCode.FORBIDDEN);
    }

    public static <T> ApiResponse<T> conflict(String message) {
        return new ApiResponse<>(null, message, ResponseCode.CONFLICT);
    }

    public static <T> ApiResponse<T> noContent(String message) {
        return new ApiResponse<>(null, message, ResponseCode.NO_CONTENT);
    }
}
