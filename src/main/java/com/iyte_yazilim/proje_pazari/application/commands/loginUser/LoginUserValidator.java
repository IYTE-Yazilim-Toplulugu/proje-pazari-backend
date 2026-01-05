package com.iyte_yazilim.proje_pazari.application.commands.loginUser;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IValidator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Validator for {@link LoginUserCommand} authentication requests.
 *
 * <p>Performs custom business rule validation beyond Jakarta Bean Validation annotations. Currently
 * relies on annotation-based validation.
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2024-01-01
 * @see LoginUserCommand
 * @see LoginUserHandler
 */
@Component
public class LoginUserValidator implements IValidator<LoginUserCommand> {

    /**
     * Validates the login command for business rule compliance.
     *
     * @param command the login command to validate
     * @return array of error messages, empty if validation passes
     */
    @Override
    public String[] validate(LoginUserCommand command) {

        List<String> errors = new ArrayList<>();

        if (command.email() == null || command.email().isBlank()
                || !command.email().matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
            errors.add("Invalid email format");
        }
        if (command.password() == null || command.password().isBlank()) {
            errors.add("Password cannot be empty");
        }

        return errors.toArray(new String[0]);
    }
}
