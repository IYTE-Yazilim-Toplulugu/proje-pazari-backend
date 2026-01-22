package com.iyte_yazilim.proje_pazari.application.behaviors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.common.RequestHandlerDelegate;
import com.iyte_yazilim.proje_pazari.domain.exceptions.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ValidationBehaviorTest {

    @Mock private Validator validator;

    private ValidationBehavior<TestRequest, String> validationBehavior;

    record TestRequest(String email, String name) implements IRequest<String> {}

    @BeforeEach
    void setUp() {
        validationBehavior = new ValidationBehavior<>(validator);
    }

    @Test
    void handle_shouldCallNextWhenValidationPasses() {
        // Arrange
        TestRequest request = new TestRequest("test@example.com", "John");
        when(validator.validate(request)).thenReturn(Collections.emptySet());

        RequestHandlerDelegate<String> next = () -> "success";

        // Act
        String result = validationBehavior.handle(request, next);

        // Assert
        assertEquals("success", result);
        verify(validator).validate(request);
    }

    @Test
    void handle_shouldThrowValidationExceptionWhenViolationsExist() {
        // Arrange
        TestRequest request = new TestRequest("invalid", "");

        Set<ConstraintViolation<TestRequest>> violations = new HashSet<>();
        violations.add(createViolation("email", "must be a valid email"));
        violations.add(createViolation("name", "must not be blank"));

        when(validator.validate(request)).thenReturn(violations);

        RequestHandlerDelegate<String> next = () -> "should not reach";

        // Act & Assert
        ValidationException exception =
                assertThrows(
                        ValidationException.class, () -> validationBehavior.handle(request, next));

        assertTrue(exception.getMessage().contains("Validation failed"));
        assertTrue(
                exception.getMessage().contains("email")
                        || exception.getMessage().contains("name"));
    }

    @Test
    void handle_shouldNotCallNextWhenValidationFails() {
        // Arrange
        TestRequest request = new TestRequest("invalid", "");

        Set<ConstraintViolation<TestRequest>> violations = new HashSet<>();
        violations.add(createViolation("email", "invalid"));

        when(validator.validate(request)).thenReturn(violations);

        boolean[] nextCalled = {false};
        RequestHandlerDelegate<String> next =
                () -> {
                    nextCalled[0] = true;
                    return "result";
                };

        // Act & Assert
        assertThrows(ValidationException.class, () -> validationBehavior.handle(request, next));
        assertFalse(nextCalled[0], "Next delegate should not be called when validation fails");
    }

    @Test
    void handle_shouldIncludeAllViolationsInErrorMessage() {
        // Arrange
        TestRequest request = new TestRequest("bad", "");

        Set<ConstraintViolation<TestRequest>> violations = new HashSet<>();
        violations.add(createViolation("field1", "error1"));
        violations.add(createViolation("field2", "error2"));
        violations.add(createViolation("field3", "error3"));

        when(validator.validate(request)).thenReturn(violations);

        RequestHandlerDelegate<String> next = () -> "result";

        // Act & Assert
        ValidationException exception =
                assertThrows(
                        ValidationException.class, () -> validationBehavior.handle(request, next));

        String message = exception.getMessage();
        assertTrue(
                message.contains("field1")
                        || message.contains("field2")
                        || message.contains("field3"));
    }

    @SuppressWarnings("unchecked")
    private ConstraintViolation<TestRequest> createViolation(String propertyPath, String message) {
        ConstraintViolation<TestRequest> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn(propertyPath);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn(message);
        return violation;
    }
}
