package com.iyte_yazilim.proje_pazari.application.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ApiResponseTest {

    @Test
    @DisplayName("Should create success response with data and message")
    void shouldCreateSuccessResponse() {
        // When
        ApiResponse<String> response = ApiResponse.success("test-data", "Operation successful");

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("test-data", response.getData());
        assertEquals("Operation successful", response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    @DisplayName("Should create created response")
    void shouldCreateCreatedResponse() {
        // When
        ApiResponse<String> response = ApiResponse.created("new-item", "Item created");

        // Then
        assertEquals(ResponseCode.CREATED, response.getCode());
        assertEquals("new-item", response.getData());
        assertEquals("Item created", response.getMessage());
    }

    @Test
    @DisplayName("Should create accepted response")
    void shouldCreateAcceptedResponse() {
        // When
        ApiResponse<String> response = ApiResponse.accepted("pending", "Request accepted");

        // Then
        assertEquals(ResponseCode.ACCEPTED, response.getCode());
        assertEquals("pending", response.getData());
    }

    @Test
    @DisplayName("Should create bad request response without data")
    void shouldCreateBadRequestResponse() {
        // When
        ApiResponse<String> response = ApiResponse.badRequest("Invalid input");

        // Then
        assertEquals(ResponseCode.BAD_REQUEST, response.getCode());
        assertNull(response.getData());
        assertEquals("Invalid input", response.getMessage());
    }

    @Test
    @DisplayName("Should create not found response")
    void shouldCreateNotFoundResponse() {
        // When
        ApiResponse<String> response = ApiResponse.notFound("Resource not found");

        // Then
        assertEquals(ResponseCode.NOT_FOUND, response.getCode());
        assertNull(response.getData());
        assertEquals("Resource not found", response.getMessage());
    }

    @Test
    @DisplayName("Should create unauthorized response")
    void shouldCreateUnauthorizedResponse() {
        // When
        ApiResponse<String> response = ApiResponse.unauthorized("Not authenticated");

        // Then
        assertEquals(ResponseCode.UNAUTHORIZED, response.getCode());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("Should create forbidden response")
    void shouldCreateForbiddenResponse() {
        // When
        ApiResponse<String> response = ApiResponse.forbidden("Access denied");

        // Then
        assertEquals(ResponseCode.FORBIDDEN, response.getCode());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("Should create conflict response")
    void shouldCreateConflictResponse() {
        // When
        ApiResponse<String> response = ApiResponse.conflict("Resource already exists");

        // Then
        assertEquals(ResponseCode.CONFLICT, response.getCode());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("Should create no content response")
    void shouldCreateNoContentResponse() {
        // When
        ApiResponse<String> response = ApiResponse.noContent("No content available");

        // Then
        assertEquals(ResponseCode.NO_CONTENT, response.getCode());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("Should create validation error response")
    void shouldCreateValidationErrorResponse() {
        // When
        ApiResponse<String> response = ApiResponse.validationError("Validation failed");

        // Then
        assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("Should create internal server error response")
    void shouldCreateInternalErrorResponse() {
        // When
        ApiResponse<String> response = ApiResponse.internalError("Server error");

        // Then
        assertEquals(ResponseCode.INTERNAL_SERVER_ERROR, response.getCode());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("Should create generic error response")
    void shouldCreateGenericErrorResponse() {
        // When
        ApiResponse<String> response = ApiResponse.error("Something went wrong");

        // Then
        assertEquals(ResponseCode.INTERNAL_SERVER_ERROR, response.getCode());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("Should include timestamp in all responses")
    void shouldIncludeTimestamp() {
        // When
        ApiResponse<String> success = ApiResponse.success("data", "msg");
        ApiResponse<String> error = ApiResponse.badRequest("error");

        // Then
        assertNotNull(success.getTimestamp());
        assertNotNull(error.getTimestamp());
    }
}
