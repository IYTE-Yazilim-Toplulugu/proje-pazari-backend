package com.iyte_yazilim.proje_pazari.application.services;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMessageAccessPolicy {

    public boolean isParticipant(ProjectApplicationEntity application, String userId) {
        return application.getUser().getId().equals(userId)
                || application.getProject().getOwner().getId().equals(userId);
    }

    public UserEntity participant(ProjectApplicationEntity application, String userId) {
        if (application.getUser().getId().equals(userId)) {
            return application.getUser();
        }
        if (application.getProject().getOwner().getId().equals(userId)) {
            return application.getProject().getOwner();
        }
        throw new IllegalArgumentException("User is not an application participant");
    }
}
