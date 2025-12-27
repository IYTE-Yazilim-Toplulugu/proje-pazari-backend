package com.iyte_yazilim.proje_pazari.application.commands.createProject;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IValidator;
import java.util.ArrayList;
import org.springframework.stereotype.Component;

@Component
public class CreateProjectValidator implements IValidator<CreateProjectCommand> {

    @Override
    public String[] validate(CreateProjectCommand command) {

        ArrayList<String> errors = new ArrayList<>();

        if (command.projectName() == null || command.projectName().isEmpty()) {
            errors.add("Project name is required.");
        }
        if (command.ownerId() == null || command.ownerId().isEmpty()) {
            errors.add("Owner ID is required.");
        }
        return errors.toArray(new String[0]);
    }
}
