package com.iyte_yazilim.proje_pazari.application.commands.registerUser;

import org.springframework.stereotype.Component;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IValidator;

@Component
public class RegisterUserValidator implements IValidator<RegisterUserCommand> {

    @Override
    public String[] validate(RegisterUserCommand command) {
        // Additional custom validation beyond Jakarta validation annotations
        // For now, relying on Jakarta validation
        return new String[0];
    }
}
