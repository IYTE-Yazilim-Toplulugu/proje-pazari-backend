package com.iyte_yazilim.proje_pazari.domain.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation for IYTE email addresses.
 *
 * <p>Validates that the email address belongs to one of the allowed IYTE domains:
 *
 * @std.iyte.edu.tr or @iyte.edu.tr
 *     <p>This annotation delegates validation to the IyteEmail Value Object, ensuring domain
 *     validation logic stays within the domain layer.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IyteEmailValidator.class)
public @interface ValidIyteEmail {
    String message() default "Email must be from @std.iyte.edu.tr or @iyte.edu.tr domain";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
