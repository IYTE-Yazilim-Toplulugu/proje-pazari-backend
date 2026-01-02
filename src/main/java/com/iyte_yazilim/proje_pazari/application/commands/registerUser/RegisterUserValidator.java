package com.iyte_yazilim.proje_pazari.application.commands.registerUser;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IValidator;
import com.iyte_yazilim.proje_pazari.domain.models.IyteEmail;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RegisterUserValidator implements IValidator<RegisterUserCommand> {

    @Override
    public String[] validate(RegisterUserCommand command) {
        List<String> errors = new ArrayList<>();

        // Validate IYTE email using Value Object
        try {
            IyteEmail.of(command.email());
        } catch (IllegalArgumentException e) {
            errors.add(e.getMessage());
        }

        return errors.toArray(new String[0]);
    }
}
