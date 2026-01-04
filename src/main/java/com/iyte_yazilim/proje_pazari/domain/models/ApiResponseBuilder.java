package com.iyte_yazilim.proje_pazari.domain.models;

import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Builder class for creating localized ApiResponse objects.
 *
 * <p>This builder provides a convenient way to create API responses with
 * internationalized messages using message codes instead of hardcoded strings.
 *
 * <p>Features:
 * <ul>
 *   <li>Automatic message resolution via MessageService</li>
 *   <li>Support for parameterized messages</li>
 *   <li>Maintains existing ApiResponse structure</li>
 *   <li>Type-safe response creation</li>
 * </ul>
 *
 * <p>Usage examples:
 * <pre>
 * {@code
 * // Success response with simple message
 * ApiResponse<User> response = apiResponseBuilder.success(user, "user.created.success");
 *
 * // Success response with parameterized message
 * ApiResponse<User> response = apiResponseBuilder.success(
 *     user,
 *     "user.welcome",
 *     userName
 * );
 *
 * // Error response
 * ApiResponse<Void> response = apiResponseBuilder.error(
 *     ResponseCode.NOT_FOUND,
 *     "user.not.found.with.id",
 *     userId
 * );
 *
 * // Validation error
 * ApiResponse<Void> response = apiResponseBuilder.validationError(
 *     "validation.email.invalid"
 * );
 * }
 * </pre>
 *
 * @see ApiResponse
 * @see MessageService
 * @see ResponseCode
 */
@Component
@RequiredArgsConstructor
public class ApiResponseBuilder {

    private final MessageService messageService;

    // ==================== SUCCESS RESPONSES ====================

    /**
     * Creates a successful response with localized message.
     *
     * @param <T> the type of data in the response
     * @param data the response data
     * @param messageCode the message code for localization
     * @return ApiResponse with SUCCESS code and localized message
     *
     * @example
     * <pre>
     * ApiResponse<User> response = apiResponseBuilder.success(user, "user.created.success");
     * // Returns: {data: user, message: "User created successfully", code: SUCCESS}
     * </pre>
     */
    public <T> ApiResponse<T> success(T data, String messageCode) {
        String message = messageService.getMessage(messageCode);
        return ApiResponse.success(data, message);
    }

    /**
     * Creates a successful response with parameterized localized message.
     *
     * @param <T> the type of data in the response
     * @param data the response data
     * @param messageCode the message code for localization
     * @param args arguments to substitute into the message
     * @return ApiResponse with SUCCESS code and localized message with parameters
     *
     * @example
     * <pre>
     * ApiResponse<User> response = apiResponseBuilder.success(
     *     user,
     *     "user.welcome.with.name",
     *     "John"
     * );
     * // Returns: {data: user, message: "Welcome, John!", code: SUCCESS}
     * </pre>
     */
    public <T> ApiResponse<T> success(T data, String messageCode, Object... args) {
        String message = messageService.getMessage(messageCode, args);
        return ApiResponse.success(data, message);
    }

    /**
     * Creates a successful CREATED (201) response with localized message.
     *
     * @param <T> the type of data in the response
     * @param data the created resource data
     * @param messageCode the message code for localization
     * @return ApiResponse with CREATED code and localized message
     *
     * @example
     * <pre>
     * ApiResponse<Project> response = apiResponseBuilder.created(project, "project.created.success");
     * // Returns: {data: project, message: "Project created successfully", code: CREATED}
     * </pre>
     */
    public <T> ApiResponse<T> created(T data, String messageCode) {
        String message = messageService.getMessage(messageCode);
        return ApiResponse.created(data, message);
    }

    /**
     * Creates a successful CREATED response with parameterized localized message.
     *
     * @param <T> the type of data in the response
     * @param data the created resource data
     * @param messageCode the message code for localization
     * @param args arguments to substitute into the message
     * @return ApiResponse with CREATED code and localized message with parameters
     */
    public <T> ApiResponse<T> created(T data, String messageCode, Object... args) {
        String message = messageService.getMessage(messageCode, args);
        return ApiResponse.created(data, message);
    }

    /**
     * Creates a successful ACCEPTED (202) response with localized message.
     *
     * @param <T> the type of data in the response
     * @param data the response data
     * @param messageCode the message code for localization
     * @return ApiResponse with ACCEPTED code and localized message
     */
    public <T> ApiResponse<T> accepted(T data, String messageCode) {
        String message = messageService.getMessage(messageCode);
        return ApiResponse.accepted(data, message);
    }

    /**
     * Creates a successful ACCEPTED response with parameterized localized message.
     *
     * @param <T> the type of data in the response
     * @param data the response data
     * @param messageCode the message code for localization
     * @param args arguments to substitute into the message
     * @return ApiResponse with ACCEPTED code and localized message with parameters
     */
    public <T> ApiResponse<T> accepted(T data, String messageCode, Object... args) {
        String message = messageService.getMessage(messageCode, args);
        return ApiResponse.accepted(data, message);
    }

    /**
     * Creates a NO_CONTENT (204) response with localized message.
     *
     * @param <T> the type of data in the response
     * @param messageCode the message code for localization
     * @return ApiResponse with NO_CONTENT code and localized message
     */
    public <T> ApiResponse<T> noContent(String messageCode) {
        String message = messageService.getMessage(messageCode);
        return ApiResponse.noContent(message);
    }

    /**
     * Creates a NO_CONTENT response with parameterized localized message.
     *
     * @param <T> the type of data in the response
     * @param messageCode the message code for localization
     * @param args arguments to substitute into the message
     * @return ApiResponse with NO_CONTENT code and localized message with parameters
     */
    public <T> ApiResponse<T> noContent(String messageCode, Object... args) {
        String message = messageService.getMessage(messageCode, args);
        return ApiResponse.noContent(message);
    }

    // ==================== ERROR RESPONSES ====================

    /**
     * Creates an error response with specified response code and localized message.
     *
     * @param <T> the type of data in the response
     * @param code the response code (e.g., BAD_REQUEST, NOT_FOUND)
     * @param messageCode the message code for localization
     * @return ApiResponse with specified code and localized message
     *
     * @example
     * <pre>
     * ApiResponse<Void> response = apiResponseBuilder.error(
     *     ResponseCode.NOT_FOUND,
     *     "user.not.found"
     * );
     * // Returns: {data: null, message: "User not found", code: NOT_FOUND}
     * </pre>
     */
    public <T> ApiResponse<T> error(ResponseCode code, String messageCode) {
        String message = messageService.getMessage(messageCode);
        return createErrorResponse(code, message);
    }

    /**
     * Creates an error response with specified response code and parameterized localized message.
     *
     * @param <T> the type of data in the response
     * @param code the response code
     * @param messageCode the message code for localization
     * @param args arguments to substitute into the message
     * @return ApiResponse with specified code and localized message with parameters
     *
     * @example
     * <pre>
     * ApiResponse<Void> response = apiResponseBuilder.error(
     *     ResponseCode.NOT_FOUND,
     *     "user.not.found.with.id",
     *     "123"
     * );
     * // Returns: {data: null, message: "User with ID 123 not found", code: NOT_FOUND}
     * </pre>
     */
    public <T> ApiResponse<T> error(ResponseCode code, String messageCode, Object... args) {
        String message = messageService.getMessage(messageCode, args);
        return createErrorResponse(code, message);
    }

    /**
     * Creates a BAD_REQUEST (400) error response with localized message.
     *
     * @param <T> the type of data in the response
     * @param messageCode the message code for localization
     * @return ApiResponse with BAD_REQUEST code and localized message
     */
    public <T> ApiResponse<T> badRequest(String messageCode) {
        String message = messageService.getMessage(messageCode);
        return ApiResponse.badRequest(message);
    }

    /**
     * Creates a BAD_REQUEST error response with parameterized localized message.
     *
     * @param <T> the type of data in the response
     * @param messageCode the message code for localization
     * @param args arguments to substitute into the message
     * @return ApiResponse with BAD_REQUEST code and localized message with parameters
     */
    public <T> ApiResponse<T> badRequest(String messageCode, Object... args) {
        String message = messageService.getMessage(messageCode, args);
        return ApiResponse.badRequest(message);
    }

    /**
     * Creates an UNAUTHORIZED (401) error response with localized message.
     *
     * @param <T> the type of data in the response
     * @param messageCode the message code for localization
     * @return ApiResponse with UNAUTHORIZED code and localized message
     */
    public <T> ApiResponse<T> unauthorized(String messageCode) {
        String message = messageService.getMessage(messageCode);
        return ApiResponse.unauthorized(message);
    }

    /**
     * Creates an UNAUTHORIZED error response with parameterized localized message.
     *
     * @param <T> the type of data in the response
     * @param messageCode the message code for localization
     * @param args arguments to substitute into the message
     * @return ApiResponse with UNAUTHORIZED code and localized message with parameters
     */
    public <T> ApiResponse<T> unauthorized(String messageCode, Object... args) {
        String message = messageService.getMessage(messageCode, args);
        return ApiResponse.unauthorized(message);
    }

    /**
     * Creates a FORBIDDEN (403) error response with localized message.
     *
     * @param <T> the type of data in the response
     * @param messageCode the message code for localization
     * @return ApiResponse with FORBIDDEN code and localized message
     */
    public <T> ApiResponse<T> forbidden(String messageCode) {
        String message = messageService.getMessage(messageCode);
        return ApiResponse.forbidden(message);
    }

    /**
     * Creates a FORBIDDEN error response with parameterized localized message.
     *
     * @param <T> the type of data in the response
     * @param messageCode the message code for localization
     * @param args arguments to substitute into the message
     * @return ApiResponse with FORBIDDEN code and localized message with parameters
     */
    public <T> ApiResponse<T> forbidden(String messageCode, Object... args) {
        String message = messageService.getMessage(messageCode, args);
        return ApiResponse.forbidden(message);
    }

    /**
     * Creates a NOT_FOUND (404) error response with localized message.
     *
     * @param <T> the type of data in the response
     * @param messageCode the message code for localization
     * @return ApiResponse with NOT_FOUND code and localized message
     */
    public <T> ApiResponse<T> notFound(String messageCode) {
        String message = messageService.getMessage(messageCode);
        return ApiResponse.notFound(message);
    }

    /**
     * Creates a NOT_FOUND error response with parameterized localized message.
     *
     * @param <T> the type of data in the response
     * @param messageCode the message code for localization
     * @param args arguments to substitute into the message
     * @return ApiResponse with NOT_FOUND code and localized message with parameters
     */
    public <T> ApiResponse<T> notFound(String messageCode, Object... args) {
        String message = messageService.getMessage(messageCode, args);
        return ApiResponse.notFound(message);
    }

    /**
     * Creates a CONFLICT (409) error response with localized message.
     *
     * @param <T> the type of data in the response
     * @param messageCode the message code for localization
     * @return ApiResponse with CONFLICT code and localized message
     */
    public <T> ApiResponse<T> conflict(String messageCode) {
        String message = messageService.getMessage(messageCode);
        return ApiResponse.conflict(message);
    }

    /**
     * Creates a CONFLICT error response with parameterized localized message.
     *
     * @param <T> the type of data in the response
     * @param messageCode the message code for localization
     * @param args arguments to substitute into the message
     * @return ApiResponse with CONFLICT code and localized message with parameters
     */
    public <T> ApiResponse<T> conflict(String messageCode, Object... args) {
        String message = messageService.getMessage(messageCode, args);
        return ApiResponse.conflict(message);
    }

    /**
     * Creates a VALIDATION_ERROR response with localized message.
     *
     * @param <T> the type of data in the response
     * @param messageCode the message code for localization
     * @return ApiResponse with VALIDATION_ERROR code and localized message
     */
    public <T> ApiResponse<T> validationError(String messageCode) {
        String message = messageService.getMessage(messageCode);
        return ApiResponse.validationError(message);
    }

    /**
     * Creates a VALIDATION_ERROR response with parameterized localized message.
     *
     * @param <T> the type of data in the response
     * @param messageCode the message code for localization
     * @param args arguments to substitute into the message
     * @return ApiResponse with VALIDATION_ERROR code and localized message with parameters
     */
    public <T> ApiResponse<T> validationError(String messageCode, Object... args) {
        String message = messageService.getMessage(messageCode, args);
        return ApiResponse.validationError(message);
    }

    /**
     * Creates an INTERNAL_SERVER_ERROR (500) response with localized message.
     *
     * @param <T> the type of data in the response
     * @param messageCode the message code for localization
     * @return ApiResponse with INTERNAL_SERVER_ERROR code and localized message
     */
    public <T> ApiResponse<T> internalError(String messageCode) {
        String message = messageService.getMessage(messageCode);
        return ApiResponse.internalError(message);
    }

    /**
     * Creates an INTERNAL_SERVER_ERROR response with parameterized localized message.
     *
     * @param <T> the type of data in the response
     * @param messageCode the message code for localization
     * @param args arguments to substitute into the message
     * @return ApiResponse with INTERNAL_SERVER_ERROR code and localized message with parameters
     */
    public <T> ApiResponse<T> internalError(String messageCode, Object... args) {
        String message = messageService.getMessage(messageCode, args);
        return ApiResponse.internalError(message);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Helper method to create error responses based on ResponseCode.
     *
     * @param <T> the type of data in the response
     * @param code the response code
     * @param message the resolved message
     * @return ApiResponse with appropriate code and message
     */
    private <T> ApiResponse<T> createErrorResponse(ResponseCode code, String message) {
        return switch (code) {
            case BAD_REQUEST -> ApiResponse.badRequest(message);
            case UNAUTHORIZED -> ApiResponse.unauthorized(message);
            case FORBIDDEN -> ApiResponse.forbidden(message);
            case NOT_FOUND -> ApiResponse.notFound(message);
            case CONFLICT -> ApiResponse.conflict(message);
            case VALIDATION_ERROR -> ApiResponse.validationError(message);
            case INTERNAL_SERVER_ERROR -> ApiResponse.internalError(message);
            case NO_CONTENT -> ApiResponse.noContent(message);
            default -> ApiResponse.error(message);
        };
    }
}