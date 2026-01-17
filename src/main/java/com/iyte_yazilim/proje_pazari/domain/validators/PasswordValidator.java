package com.iyte_yazilim.proje_pazari.domain.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for {@link ValidPassword} annotation.
 *
 * <p>Enforces strong password policy by checking:
 *
 * <ul>
 *   <li>Minimum 8 characters
 *   <li>At least one uppercase letter
 *   <li>At least one lowercase letter
 *   <li>At least one digit
 *   <li>At least one special character (@$!%*?&)
 * </ul>
 *
 * <p>This provides detailed error messages for each failed requirement.
 */
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            return true; // null/blank values are handled by @NotBlank
        }

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (password.length() < 8) {
            context.buildConstraintViolationWithTemplate(
                            "Password must be at least 8 characters long")
                    .addConstraintViolation();
            valid = false;
        }

        if (!containsUpperCase(password)) {
            context.buildConstraintViolationWithTemplate(
                            "Password must contain at least one uppercase letter")
                    .addConstraintViolation();
            valid = false;
        }

        if (!containsLowerCase(password)) {
            context.buildConstraintViolationWithTemplate(
                            "Password must contain at least one lowercase letter")
                    .addConstraintViolation();
            valid = false;
        }

        if (!containsDigit(password)) {
            context.buildConstraintViolationWithTemplate("Password must contain at least one digit")
                    .addConstraintViolation();
            valid = false;
        }

        if (!containsSpecialCharacter(password)) {
            context.buildConstraintViolationWithTemplate(
                            "Password must contain at least one special character (@$!%*?&)")
                    .addConstraintViolation();
            valid = false;
        }

        return valid;
    }

    private boolean containsUpperCase(String password) {
        return password.chars().anyMatch(Character::isUpperCase);
    }

    private boolean containsLowerCase(String password) {
        return password.chars().anyMatch(Character::isLowerCase);
    }

    private boolean containsDigit(String password) {
        return password.chars().anyMatch(Character::isDigit);
    }

    private boolean containsSpecialCharacter(String password) {
        return password.chars().anyMatch(ch -> "@$!%*?&".indexOf(ch) >= 0);
    }
}
