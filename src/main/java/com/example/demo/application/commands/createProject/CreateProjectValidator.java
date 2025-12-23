package com.example.demo.application.commands.createProject;

import java.util.ArrayList;

import com.example.demo.domain.interfaces.IValidator;

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
