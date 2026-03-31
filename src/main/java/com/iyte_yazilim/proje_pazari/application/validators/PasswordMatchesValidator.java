package com.iyte_yazilim.proje_pazari.application.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Validates that {@code newPassword} and {@code confirmPassword} are equal. */
public class PasswordMatchesValidator
        implements ConstraintValidator<PasswordMatches, PasswordMatchable> {

    @Override
    public boolean isValid(PasswordMatchable command, ConstraintValidatorContext context) {
        if (command == null) {
            return true;
        }
        String pwd = command.newPassword();
        return pwd != null && pwd.equals(command.confirmPassword());
    }
}
