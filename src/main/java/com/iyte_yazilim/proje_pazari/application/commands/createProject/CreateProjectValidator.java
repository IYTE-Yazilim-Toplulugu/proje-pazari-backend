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

        if (command.deadline() != null && command.deadline().isBefore(LocalDateTime.now())) {
            errors.add("Deadline cannot be in the past.");
        }

        return errors.toArray(new String[0]);
    }
}
