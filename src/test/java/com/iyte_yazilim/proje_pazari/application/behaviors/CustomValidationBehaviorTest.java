package com.iyte_yazilim.proje_pazari.application.behaviors;

import static org.junit.jupiter.api.Assertions.*;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.common.RequestHandlerDelegate;
import com.iyte_yazilim.proje_pazari.application.exceptions.ValidationException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IValidator;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class CustomValidationBehaviorTest {

    record TestRequest(String value) implements IRequest<String> {}

    record OtherRequest(String value) implements IRequest<String> {}

    static class TestRequestValidator implements IValidator<TestRequest> {
        private final String[] result;

        TestRequestValidator(String... result) {
            this.result = result;
        }

        @Override
        public String[] validate(TestRequest command) {
            return result;
        }
    }

    static class NullReturningValidator implements IValidator<TestRequest> {
        @Override
        public String[] validate(TestRequest command) {
            return null;
        }
    }

    static class OtherRequestValidator implements IValidator<OtherRequest> {
        @Override
        public String[] validate(OtherRequest command) {
            return new String[] {"should not trigger"};
        }
    }

    @Test
    void handle_shouldCallNextWhenNoValidatorMatches() {
        CustomValidationBehavior<TestRequest, String> behavior =
                new CustomValidationBehavior<>(Collections.emptyList());
        behavior.initValidatorCache();

        RequestHandlerDelegate<String> next = () -> "success";

        String result = behavior.handle(new TestRequest("test"), next);

        assertEquals("success", result);
    }

    @Test
    void handle_shouldCallNextWhenValidatorReturnsEmptyArray() {
        CustomValidationBehavior<TestRequest, String> behavior =
                new CustomValidationBehavior<>(List.of(new TestRequestValidator()));
        behavior.initValidatorCache();

        RequestHandlerDelegate<String> next = () -> "success";

        String result = behavior.handle(new TestRequest("test"), next);

        assertEquals("success", result);
    }

    @Test
    void handle_shouldCallNextWhenValidatorReturnsNull() {
        CustomValidationBehavior<TestRequest, String> behavior =
                new CustomValidationBehavior<>(List.of(new NullReturningValidator()));
        behavior.initValidatorCache();

        RequestHandlerDelegate<String> next = () -> "success";

        String result = behavior.handle(new TestRequest("test"), next);

        assertEquals("success", result);
    }

    @Test
    void handle_shouldThrowValidationExceptionWhenValidatorReturnsErrors() {
        CustomValidationBehavior<TestRequest, String> behavior =
                new CustomValidationBehavior<>(
                        List.of(new TestRequestValidator("field is required")));
        behavior.initValidatorCache();

        RequestHandlerDelegate<String> next = () -> "should not reach";

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> behavior.handle(new TestRequest("test"), next));

        assertTrue(exception.getMessage().contains("Validation failed"));
        assertTrue(exception.getMessage().contains("field is required"));
    }

    @Test
    void handle_shouldNotCallNextWhenValidationFails() {
        CustomValidationBehavior<TestRequest, String> behavior =
                new CustomValidationBehavior<>(List.of(new TestRequestValidator("error")));
        behavior.initValidatorCache();

        boolean[] nextCalled = {false};
        RequestHandlerDelegate<String> next =
                () -> {
                    nextCalled[0] = true;
                    return "result";
                };

        assertThrows(
                ValidationException.class, () -> behavior.handle(new TestRequest("test"), next));
        assertFalse(nextCalled[0], "Next delegate should not be called when validation fails");
    }

    @Test
    void handle_shouldIncludeAllErrorsInMessage() {
        CustomValidationBehavior<TestRequest, String> behavior =
                new CustomValidationBehavior<>(
                        List.of(new TestRequestValidator("error1", "error2", "error3")));
        behavior.initValidatorCache();

        RequestHandlerDelegate<String> next = () -> "result";

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> behavior.handle(new TestRequest("test"), next));

        String message = exception.getMessage();
        assertTrue(message.contains("error1"));
        assertTrue(message.contains("error2"));
        assertTrue(message.contains("error3"));
    }

    @Test
    void handle_shouldOnlyRunMatchingValidators() {
        @SuppressWarnings("unchecked")
        CustomValidationBehavior<TestRequest, String> behavior =
                new CustomValidationBehavior<>(
                        List.of((IValidator<?>) new OtherRequestValidator()));
        behavior.initValidatorCache();

        RequestHandlerDelegate<String> next = () -> "success";

        String result = behavior.handle(new TestRequest("test"), next);

        assertEquals("success", result);
    }
}
