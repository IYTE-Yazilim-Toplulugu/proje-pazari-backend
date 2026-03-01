package com.iyte_yazilim.proje_pazari.application.commands.adminReviewApplication;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminReviewApplicationHandler
        implements IRequestHandler<AdminReviewApplicationCommand, ApiResponse<Void>> {

    private final ProjectApplicationRepository applicationRepository;

    @Override
    @Transactional
    public ApiResponse<Void> handle(AdminReviewApplicationCommand command) {
        ProjectApplicationEntity application =
                applicationRepository.findById(command.applicationId()).orElse(null);

        if (application == null) {
            return ApiResponse.notFound(
                    "Application not found with id: " + command.applicationId());
        }

        application.setStatus(command.status());
        applicationRepository.save(application);

        return ApiResponse.success(
                null, "Application " + command.status().getStatus() + " successfully");
    }
}
