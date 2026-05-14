package com.iyte_yazilim.proje_pazari.presentation.config;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ErrorCode;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.exceptions.DomainException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

/**
 * Translates exceptions into standardized {@link ApiResponse} error bodies.
 *
 * <p>Two invariants every handler upholds:
 *
 * <ul>
 *   <li>the response {@code message} is always resolved from the message bundle — localized and
 *       safe to show users; raw {@code ex.getMessage()} is never sent to the client;
 *   <li>technical detail (identifiers, emails, stack traces) goes to the server log only.
 * </ul>
 *
 * <p>Domain-level failures are all handled by a single {@link #handleDomainException} method — the
 * specific {@link ErrorCode} is carried by the {@link DomainException} itself.
 */
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final MessageService messageService;

    /**
     * Builds a localized, user-safe error response for the given {@link ErrorCode}. The user
     * message is resolved from the message bundle; the HTTP status comes from the error code's
     * category.
     */
    private ResponseEntity<ApiResponse<Void>> respond(ErrorCode errorCode) {
        ApiResponse<Void> body =
                ApiResponse.failure(
                        errorCode, messageService.getMessage(errorCode.getMessageKey()));
        return ResponseEntity.status(errorCode.getCategory().httpStatus()).body(body);
    }

    // --- Domain exceptions: one handler; the ErrorCode is carried by the exception ---

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        // Technical message may contain ids/emails/internal detail — log only, never expose.
        log.warn("Domain exception [{}]: {}", errorCode, ex.getMessage());
        return respond(errorCode);
    }

    // --- Framework / Spring exceptions ---

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        log.warn("Request body validation failed: {}", ex.getMessage());
        return respond(ErrorCode.VALIDATION_ERROR);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException ex) {
        log.warn("Authentication error: {}", ex.getMessage());
        return respond(ErrorCode.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return respond(ErrorCode.ACCESS_DENIED);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex) {
        log.warn("File upload size exceeded: {}", ex.getMessage());
        return respond(ErrorCode.FILE_TOO_LARGE);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMultipartException(MultipartException ex) {
        log.warn("Multipart request error: {}", ex.getMessage());
        return respond(ErrorCode.MALFORMED_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        log.warn("Request body not readable: {}", ex.getMessage());
        return respond(ErrorCode.MALFORMED_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex) {
        log.warn("Missing request parameter: {}", ex.getMessage());
        return respond(ErrorCode.MISSING_PARAMETER);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        log.error("Request parameter type mismatch: {}", ex.getMessage());
        ApiResponse<Void> response =
                ApiResponse.badRequest(messageService.getMessage("error.validation"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());
        return respond(ErrorCode.VALIDATION_ERROR);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex) {
        log.warn("Handler method validation error: {}", ex.getMessage());
        return respond(ErrorCode.VALIDATION_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        // Raw entity/internal text may be in the message — log only, return a generic message.
        log.warn("Illegal argument: {}", ex.getMessage());
        return respond(ErrorCode.INVALID_ARGUMENT);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException ex) {
        log.error("Illegal state: {}", ex.getMessage());
        return respond(ErrorCode.INTERNAL_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return respond(ErrorCode.INTERNAL_ERROR);
    }
}
