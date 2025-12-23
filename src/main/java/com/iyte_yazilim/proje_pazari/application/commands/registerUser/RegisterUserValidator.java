package com.iyte_yazilim.proje_pazari.application.commands.registerUser;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IValidator;

public class RegisterUserValidator implements IValidator<RegisterUserCommand> {

    @Override
    public String[] validate(RegisterUserCommand command) {
        // Additional custom validation beyond Jakarta validation annotations
        // For now, relying on Jakarta validation
        return new String[0];
    }
}
