package com.iyte_yazilim.proje_pazari.application.behaviors;

import static org.junit.jupiter.api.Assertions.*;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.common.RequestHandlerDelegate;
import com.iyte_yazilim.proje_pazari.application.common.SensitiveRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
class LoggingBehaviorTest {

    private LoggingBehavior<TestRequest, String> loggingBehavior;

    record TestRequest(String data) implements IRequest<String> {}

    record SensitiveTestRequest(String data) implements IRequest<String>, SensitiveRequest {}

    @BeforeEach
    void setUp() {
        loggingBehavior = new LoggingBehavior<>();
    }

    @Test
    void handle_shouldCallNextDelegateAndReturnResult() {
        // Arrange
        TestRequest request = new TestRequest("test data");
        RequestHandlerDelegate<String> next = () -> "result";

        // Act
        String result = loggingBehavior.handle(request, next);

        // Assert
        assertEquals("result", result);
    }

    @Test
    void handle_shouldPropagateExceptions() {
        // Arrange
        TestRequest request = new TestRequest("test");
        RuntimeException expectedException = new RuntimeException("Test exception");
        RequestHandlerDelegate<String> next =
                () -> {
                    throw expectedException;
                };

        // Act & Assert
        RuntimeException thrown =
                assertThrows(RuntimeException.class, () -> loggingBehavior.handle(request, next));
        assertEquals("Test exception", thrown.getMessage());
    }

    @Test
    void handle_shouldNotLogSensitiveExceptionMessages(CapturedOutput output) {
        String secret = "MESSAGE_BODY_SECRET";
        SensitiveTestRequest request = new SensitiveTestRequest(secret);
        LoggingBehavior<SensitiveTestRequest, String> sensitiveLoggingBehavior =
                new LoggingBehavior<>();
        RequestHandlerDelegate<String> next =
                () -> {
                    throw new RuntimeException(secret);
                };

        assertThrows(RuntimeException.class, () -> sensitiveLoggingBehavior.handle(request, next));

        assertFalse(output.getAll().contains(secret));
        assertTrue(output.getAll().contains("RuntimeException"));
    }

    @Test
    void handle_shouldMeasureExecutionTime() {
        // Arrange
        TestRequest request = new TestRequest("test");
        RequestHandlerDelegate<String> next =
                () -> {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return "delayed result";
                };

        // Act
        long startTime = System.currentTimeMillis();
        String result = loggingBehavior.handle(request, next);
        long endTime = System.currentTimeMillis();

        // Assert
        assertEquals("delayed result", result);
        assertTrue(
                endTime - startTime >= 50,
                "Execution should take at least 50ms due to Thread.sleep");
    }

    @Test
    void handle_shouldWorkWithNullReturnValue() {
        // Arrange
        TestRequest request = new TestRequest("test");
        RequestHandlerDelegate<String> next = () -> null;

        // Act
        String result = loggingBehavior.handle(request, next);

        // Assert
        assertNull(result);
    }
}
