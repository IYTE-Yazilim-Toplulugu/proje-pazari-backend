package com.iyte_yazilim.proje_pazari.application.behaviors;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.common.RequestHandlerDelegate;
import com.iyte_yazilim.proje_pazari.application.exceptions.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Pipeline behavior that validates requests using Jakarta Bean Validation.
 *
 * <p>Validates the request before passing it to the handler. If validation fails, throws a
 * ValidationException.
 */
@Component
@Order(2)
@RequiredArgsConstructor
public class ValidationBehavior<TRequest extends IRequest<TResponse>, TResponse>
        implements IPipelineBehavior<TRequest, TResponse> {

    private final Validator validator;

    @Override
    public TResponse handle(TRequest request, RequestHandlerDelegate<TResponse> next) {
        Set<ConstraintViolation<TRequest>> violations = validator.validate(request);

        if (!violations.isEmpty()) {
            String errors =
                    violations.stream()
                            .map(v -> v.getPropertyPath().toString() + ": " + v.getMessage())
                            .collect(Collectors.joining(", "));
            throw new ValidationException("Validation failed: " + errors);
        }

        return next.handle();
    }
}
