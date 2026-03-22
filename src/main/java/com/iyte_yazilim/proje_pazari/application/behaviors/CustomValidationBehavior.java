package com.iyte_yazilim.proje_pazari.application.behaviors;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.common.RequestHandlerDelegate;
import com.iyte_yazilim.proje_pazari.application.exceptions.ValidationException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IValidator;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Pipeline behavior that runs custom {@link IValidator} implementations.
 *
 * <p>Auto-discovers all {@link IValidator} beans and invokes the matching validator(s) for each
 * request type. Runs after Bean Validation ({@link ValidationBehavior}) and before transactions.
 */
@Slf4j
@Component
@Order(3)
public class CustomValidationBehavior<TRequest extends IRequest<TResponse>, TResponse>
        implements IPipelineBehavior<TRequest, TResponse> {

    private final List<IValidator<?>> validators;
    private final Map<Class<?>, List<IValidator<?>>> validatorCache = new ConcurrentHashMap<>();

    public CustomValidationBehavior(List<IValidator<?>> validators) {
        this.validators = validators != null ? validators : Collections.emptyList();
    }

    /**
     * Builds the validator cache by resolving the generic type parameter of each {@link IValidator}
     * bean at startup.
     *
     * <p><strong>Spring AOP proxy incompatibility:</strong> This method calls {@code
     * validator.getClass()} to resolve the parameterized type. If any {@link IValidator}
     * implementation is decorated with Spring AOP advice (e.g., {@code @Transactional} or
     * {@code @Cacheable}), {@code getClass()} returns the CGLIB proxy class rather than the
     * concrete implementation. {@code ResolvableType} cannot resolve the generic argument from a
     * proxy class, so the validator is silently skipped (a WARN is logged) and validation does not
     * run for its target command type.
     *
     * <p>To avoid this: do not place AOP-triggering annotations directly on {@link IValidator}
     * implementations. If database access is required inside a validator, inject a repository
     * directly without wrapping the validator itself in a transactional proxy.
     */
    @PostConstruct
    @SuppressWarnings("rawtypes")
    void initValidatorCache() {
        for (IValidator<?> validator : validators) {
            ResolvableType validatorType =
                    ResolvableType.forClass(validator.getClass()).as(IValidator.class);
            Class<?> requestClass = validatorType.getGeneric(0).resolve();
            if (requestClass != null) {
                validatorCache.computeIfAbsent(requestClass, k -> new ArrayList<>()).add(validator);
            } else {
                log.warn(
                        "Could not resolve generic type for validator: {}",
                        validator.getClass().getName());
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public TResponse handle(TRequest request, RequestHandlerDelegate<TResponse> next) {
        List<IValidator<?>> matchedValidators =
                validatorCache.getOrDefault(request.getClass(), Collections.emptyList());

        List<String> allErrors = new ArrayList<>();
        for (IValidator<?> v : matchedValidators) {
            IValidator<TRequest> validator = (IValidator<TRequest>) v;
            String[] errors = validator.validate(request);
            if (errors != null) {
                for (String error : errors) {
                    if (error != null && !error.isBlank()) {
                        allErrors.add(error);
                    }
                }
            }
        }

        if (!allErrors.isEmpty()) {
            throw new ValidationException("Validation failed: " + String.join(", ", allErrors));
        }

        return next.handle();
    }
}
