package com.iyte_yazilim.proje_pazari.application.commands.loginUser;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IValidator;
import org.springframework.stereotype.Component;

@Component
public class LoginUserValidator implements IValidator<LoginUserCommand> {

    @Override
    public String[] validate(LoginUserCommand command) {
        // Additional custom validation beyond Jakarta validation annotations
        // For now, relying on Jakarta validation
        return new String[0];
    }
}
