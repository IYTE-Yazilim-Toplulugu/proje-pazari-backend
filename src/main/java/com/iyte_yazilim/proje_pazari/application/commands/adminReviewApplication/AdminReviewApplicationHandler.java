package com.iyte_yazilim.proje_pazari.application.commands.adminReviewApplication;

import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.exceptions.ApplicationNotFoundException;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
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
                applicationRepository
                        .findById(command.applicationId())
                        .orElseThrow(
                                () -> new ApplicationNotFoundException(command.applicationId()));

        application.setStatus(command.status());
        applicationRepository.save(application);

        return ApiResponse.success(
                null, "Application " + command.status().getStatus() + " successfully");
    }
}
