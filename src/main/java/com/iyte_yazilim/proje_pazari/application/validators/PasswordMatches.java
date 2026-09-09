package com.iyte_yazilim.proje_pazari.application.validators;

import com.iyte_yazilim.proje_pazari.domain.interfaces.PasswordMatchable;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class-level constraint that verifies {@code newPassword} and {@code confirmPassword} match.
 *
 * <p>Apply to any class implementing {@link PasswordMatchable}.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordMatchesValidator.class)
public @interface PasswordMatches {

    String message() default "Passwords do not match";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
