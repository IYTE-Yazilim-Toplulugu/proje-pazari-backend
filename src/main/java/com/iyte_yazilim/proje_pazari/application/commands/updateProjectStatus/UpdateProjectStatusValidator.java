package com.iyte_yazilim.proje_pazari.application.commands.updateProjectStatus;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IValidator;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class UpdateProjectStatusValidator implements IValidator<UpdateProjectStatusCommand> {

    @Override
    public String[] validate(UpdateProjectStatusCommand command) {
        List<String> errors = new ArrayList<>();

        if (command.projectId() == null || command.projectId().isBlank()) {
            errors.add("Project ID is required");
        }

        if (command.newStatus() == null) {
            errors.add("Status is required");
        }

        return errors.isEmpty() ? null : errors.toArray(new String[0]);
    }
}
