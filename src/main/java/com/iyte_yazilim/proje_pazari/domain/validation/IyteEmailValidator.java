package com.iyte_yazilim.proje_pazari.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class IyteEmailValidator implements ConstraintValidator<IyteEmail, String> {

    private static final List<String> ALLOWED_DOMAINS = List.of("@std.iyte.edu.tr", "@iyte.edu.tr");

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isBlank()) {
            return false;
        }

        return ALLOWED_DOMAINS.stream()
                .anyMatch(domain -> email.toLowerCase().endsWith(domain.toLowerCase()));
    }
}
