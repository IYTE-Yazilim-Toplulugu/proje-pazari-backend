package com.iyte_yazilim.proje_pazari.application.commands.registerUser;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IValidator;
import com.iyte_yazilim.proje_pazari.domain.models.IyteEmail;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Validator for {@link RegisterUserCommand} registration requests.
 *
 * <p>Performs custom business rule validation beyond Jakarta Bean Validation annotations. Currently
 * relies on annotation-based validation.
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2024-01-01
 * @see RegisterUserCommand
 * @see RegisterUserHandler
 */
@Component
public class RegisterUserValidator implements IValidator<RegisterUserCommand> {

    /**
     * Validates the registration command for business rule compliance.
     *
     * @param command the registration command to validate
     * @return array of error messages, empty if validation passes
     */
    @Override
    public String[] validate(RegisterUserCommand command) {
        List<String> errors = new ArrayList<>();

        // Validate IYTE email using Value Object
        try {
            IyteEmail.of(command.email());
        } catch (IllegalArgumentException e) {
            errors.add(e.getMessage());
        }

        return errors.toArray(String[]::new);
    }
}
