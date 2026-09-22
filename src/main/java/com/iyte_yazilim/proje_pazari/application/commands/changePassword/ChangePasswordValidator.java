package com.iyte_yazilim.proje_pazari.application.commands.changePassword;

import com.iyte_yazilim.proje_pazari.domain.validators.IValidator;
import org.springframework.stereotype.Component;

@Component
public class ChangePasswordValidator implements IValidator<ChangePasswordCommand> {

    @Override
    public String[] validate(ChangePasswordCommand command) {
        if (command.newPassword() != null
                && command.newPassword().equals(command.currentPassword())) {
            return new String[] {"New password must differ from current password"};
        }
        return new String[0];
    }
}
