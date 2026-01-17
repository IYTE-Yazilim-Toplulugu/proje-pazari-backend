package com.iyte_yazilim.proje_pazari.application.commands.registerUser;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IValidator;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Validator for {@link RegisterUserCommand} registration requests.
 *
 * <p>Performs custom business rule validation beyond Jakarta Bean Validation annotations.
 *
 * <p>Note: Field-level validation (email format, password strength) is handled by Jakarta Bean
 * Validation annotations like @Email and @ValidPassword. This validator is for additional business
 * logic validation.
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
     * <p>Currently delegates to Jakarta Bean Validation annotations. Additional business rules can
     * be added here if needed (e.g., checking if email domain is allowed, password not containing
     * username, etc.).
     *
     * @param command the registration command to validate
     * @return array of error messages, empty if validation passes
     */
    @Override
    public String[] validate(RegisterUserCommand command) {
        List<String> errors = new ArrayList<>();

        // Add custom business logic validation here if needed
        // Bean Validation annotations (@Email, @ValidPassword) are checked automatically by Spring

        return errors.toArray(new String[0]);
    }
}
