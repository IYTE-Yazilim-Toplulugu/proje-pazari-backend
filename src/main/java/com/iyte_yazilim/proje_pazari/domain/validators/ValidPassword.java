package com.iyte_yazilim.proje_pazari.domain.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for password validation.
 *
 * <p>Validates that a password meets security requirements:
 *
 * <ul>
 *   <li>Minimum 8 characters
 *   <li>At least one uppercase letter (A-Z)
 *   <li>At least one lowercase letter (a-z)
 *   <li>At least one digit (0-9)
 *   <li>At least one special character (@$!%*?&)
 * </ul>
 *
 * <h2>Usage Example:</h2>
 *
 * <pre>{@code
 * public record RegisterUserCommand(
 *     @ValidPassword
 *     String password
 * ) {}
 * }</pre>
 *
 * @see PasswordValidator
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
public @interface ValidPassword {

    String message() default
            "Password must be at least 8 characters and contain at least one uppercase letter, one lowercase letter, one number, and one special character (@$!%*?&)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
