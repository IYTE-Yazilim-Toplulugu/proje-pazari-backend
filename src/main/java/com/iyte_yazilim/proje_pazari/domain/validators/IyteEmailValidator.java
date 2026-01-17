package com.iyte_yazilim.proje_pazari.domain.validators;

import com.iyte_yazilim.proje_pazari.domain.models.IyteEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for @ValidIyteEmail annotation.
 *
 * <p>Delegates validation to the IyteEmail Value Object to keep domain validation logic centralized
 * in the domain layer.
 */
public class IyteEmailValidator implements ConstraintValidator<ValidIyteEmail, String> {

    @Override
    public void initialize(ValidIyteEmail constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isBlank()) {
            return false;
        }

        try {
            // Delegate to IyteEmail Value Object for validation
            IyteEmail.of(email);
            return true;
        } catch (IllegalArgumentException e) {
            // Invalid IYTE email domain
            return false;
        }
    }
}
