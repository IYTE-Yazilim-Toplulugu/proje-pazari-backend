package com.iyte_yazilim.proje_pazari.application.commands.adminReviewApplication;

import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.entities.ProjectApplication;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.exceptions.ApplicationNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectApplicationMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminReviewApplicationHandler
        implements IRequestHandler<AdminReviewApplicationCommand, ApiResponse<Void>> {

    private final ProjectApplicationRepository applicationRepository;
    private final ProjectApplicationMapper applicationMapper;

    @Override
    @Transactional
    public ApiResponse<Void> handle(AdminReviewApplicationCommand command) {
        ProjectApplicationEntity applicationEntity =
                applicationRepository
                        .findById(command.applicationId())
                        .orElseThrow(
                                () -> new ApplicationNotFoundException(command.applicationId()));

        // Delegate to domain aggregate — enforces PENDING precondition
        ProjectApplication application = applicationMapper.entityToDomain(applicationEntity);

        if (command.status() == ApplicationStatus.APPROVED) {
            application.approve();
        } else if (command.status() == ApplicationStatus.REJECTED) {
            application.reject();
        } else if (command.status() == ApplicationStatus.WITHDRAWN) {
            application.withdraw();
        } else {
            return ApiResponse.badRequest("Invalid status: " + command.status());
        }

        // Sync domain state back to persistence entity
        applicationEntity.setStatus(application.getStatus());
        applicationRepository.save(applicationEntity);

        return ApiResponse.success(
                null, "Application " + command.status().getStatus() + " successfully");
    }
}
