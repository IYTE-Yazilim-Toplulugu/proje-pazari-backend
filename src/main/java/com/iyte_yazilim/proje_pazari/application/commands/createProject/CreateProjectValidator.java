package com.iyte_yazilim.proje_pazari.application.commands.createProject;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IValidator;
import java.time.LocalDateTime;
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
        if (command.maxTeamSize() != null && command.maxTeamSize() < 1) {
            errors.add("Maximum team size must be at least 1.");
        }
        if (command.deadline() != null && command.deadline().isBefore(LocalDateTime.now())) {
            errors.add("Deadline cannot be in the past.");
        }

        return errors.toArray(new String[0]);
    }
}
