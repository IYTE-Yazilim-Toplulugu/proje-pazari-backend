package com.iyte_yazilim.proje_pazari.application.commands.updateUserProfile;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IValidator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class UpdateUserProfileValidator implements IValidator<UpdateUserProfileCommand> {

    @Override
    public String[] validate(UpdateUserProfileCommand command) {

        List<String> errors = new ArrayList<>();

        if (command.linkedinUrl() != null
                && !command.linkedinUrl().isBlank()
                && !command.linkedinUrl().matches("^https://(www\\.)?linkedin\\.com/.*")) {
            errors.add("Invalid LinkedIn URL format");
        }
        if (command.githubUrl() != null
                && !command.githubUrl().isBlank()
                && !command.githubUrl().matches("^https://github\\.com/[a-zA-Z0-9_-]+(/.*)?$")) {
            errors.add("Invalid GitHub URL format");
        }

        return errors.toArray(new String[0]);
    }
}
