package com.iyte_yazilim.proje_pazari.presentation.config;

import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final MessageService messageService;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getAllErrors()
                .forEach(
                        (error) -> {
                            String fieldName = ((FieldError) error).getField();
                            String errorMessage = error.getDefaultMessage();
                            errors.put(fieldName, errorMessage);
                        });

        ApiResponse<Map<String, String>> response =
                ApiResponse.validationError(messageService.getMessage("error.validation"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException ex) {
        log.error("Authentication error: {}", ex.getMessage());
        ApiResponse<Void> response =
                ApiResponse.unauthorized(messageService.getMessage("error.unauthorized"));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        ApiResponse<Void> response =
                ApiResponse.forbidden(messageService.getMessage("error.forbidden"));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex) {
        log.error("File upload size exceeded: {}", ex.getMessage());
        ApiResponse<Void> response =
                ApiResponse.validationError(
                        messageService.getMessage("validation.file.size.exceeded"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMultipartException(MultipartException ex) {
        log.error("Multipart request error: {}", ex.getMessage());
        ApiResponse<Void> response =
                ApiResponse.badRequest(messageService.getMessage("error.multipart.invalid"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.badRequest(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        ApiResponse<Void> response =
                ApiResponse.internalError(messageService.getMessage("error.internal"));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
