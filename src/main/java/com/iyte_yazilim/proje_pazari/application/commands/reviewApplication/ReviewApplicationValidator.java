package com.iyte_yazilim.proje_pazari.application.commands.reviewApplication;

import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.validators.IValidator;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ReviewApplicationValidator implements IValidator<ReviewApplicationCommand> {

    @Override
    public String[] validate(ReviewApplicationCommand command) {
        List<String> errors = new ArrayList<>();

        if (command.applicationId() == null || command.applicationId().isBlank()) {
            errors.add("Application ID is required");
        }

        if (command.status() == null) {
            errors.add("Status is required");
        } else if (command.status() == ApplicationStatus.PENDING) {
            errors.add("Cannot set status to PENDING during review");
        }

        return errors.isEmpty() ? null : errors.toArray(new String[0]);
    }
}
