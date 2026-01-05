package com.iyte_yazilim.proje_pazari.application.commands.registerUser;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IValidator;

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

        if (command.email() == null || command.email().isBlank()
                || !command.email().matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
            errors.add("Invalid email format");
        }
        if (command.password() == null || command.password().length() < 8) {
            errors.add("Password must be at least 8 characters long");
        }

        return errors.toArray(new String[0]);
    }
}
