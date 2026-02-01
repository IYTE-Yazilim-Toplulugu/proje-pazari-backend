package com.iyte_yazilim.proje_pazari.application.commands.submitApplication;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IValidator;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SubmitApplicationValidator implements IValidator<SubmitApplicationCommand> {

    @Override
    public String[] validate(SubmitApplicationCommand command) {
        List<String> errors = new ArrayList<>();

        if (command.projectId() == null || command.projectId().isBlank()) {
            errors.add("Project ID is required");
        }

        if (command.userId() == null || command.userId().isBlank()) {
            errors.add("User ID is required");
        }

        return errors.isEmpty() ? null : errors.toArray(new String[0]);
    }
}
