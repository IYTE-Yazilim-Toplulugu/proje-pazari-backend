package com.iyte_yazilim.proje_pazari.application.commands.loginUser;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IValidator;
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
        // Additional custom validation beyond Jakarta validation annotations
        // For now, relying on Jakarta validation
        return new String[0];
    }
}
