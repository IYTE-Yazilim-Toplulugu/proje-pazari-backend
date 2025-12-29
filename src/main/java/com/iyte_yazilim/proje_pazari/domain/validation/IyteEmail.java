package com.iyte_yazilim.proje_pazari.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IyteEmailValidator.class)
public @interface IyteEmail {
    String message() default "Email must be from @std.iyte.edu.tr or @iyte.edu.tr domain";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
