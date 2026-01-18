package com.iyte_yazilim.proje_pazari.presentation.security;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("projectSecurityChecker")
@RequiredArgsConstructor
public class ProjectSecurityChecker {

    private final ProjectRepository projectRepository;

    // Assuming ApplicationRepository and entity structure are not (yet) available
    // If it becomes available, inject it here and implement canApproveApplication
    // accordingly

    public boolean isOwner(String projectId, UserDetails userDetails) {
        if (userDetails == null) {
            return false;
        }
        return projectRepository
                .findById(projectId)
                .map(
                        project -> {
                            if (project.getOwner() == null
                                    || project.getOwner().getEmail() == null) {
                                return false;
                            }
                            return project.getOwner().getEmail().equals(userDetails.getUsername());
                        })
                .orElse(false);
    }

    // Placeholder for future implementation
    public boolean canApproveApplication(String applicationId, UserDetails userDetails) {
        // Not implemented due to missing ApplicationRepository & model/context.
        return false;
    }
}
