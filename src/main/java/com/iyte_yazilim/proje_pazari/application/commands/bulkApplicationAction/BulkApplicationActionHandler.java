package com.iyte_yazilim.proje_pazari.application.commands.bulkApplicationAction;

import com.iyte_yazilim.proje_pazari.application.dtos.BulkActionResult;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BulkApplicationActionHandler
        implements IRequestHandler<BulkApplicationActionCommand, ApiResponse<BulkActionResult>> {

    private final ProjectApplicationRepository applicationRepository;

    @Override
    @Transactional
    public ApiResponse<BulkActionResult> handle(BulkApplicationActionCommand command) {
        BulkActionResult result = new BulkActionResult();

        if (command.applicationIds() == null || command.applicationIds().isEmpty()) {
            return ApiResponse.validationError("Application IDs list cannot be empty");
        }

        for (String appId : command.applicationIds()) {
            try {
                ProjectApplicationEntity app = applicationRepository.findById(appId).orElse(null);
                if (app == null) {
                    result.addFailure(appId, "Application not found");
                    continue;
                }

                switch (command.action().toUpperCase()) {
                    case "APPROVE":
                        app.setStatus(ApplicationStatus.APPROVED);
                        applicationRepository.save(app);
                        result.incrementSuccess();
                        break;
                    case "REJECT":
                        app.setStatus(ApplicationStatus.REJECTED);
                        applicationRepository.save(app);
                        result.incrementSuccess();
                        break;
                    default:
                        result.addFailure(appId, "Unknown action: " + command.action());
                }
            } catch (Exception e) {
                result.addFailure(appId, e.getMessage());
            }
        }

        return ApiResponse.success(result, "Bulk application action completed");
    }
}
