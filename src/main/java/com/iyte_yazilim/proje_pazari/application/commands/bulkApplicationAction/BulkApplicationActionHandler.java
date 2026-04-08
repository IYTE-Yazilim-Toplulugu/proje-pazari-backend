package com.iyte_yazilim.proje_pazari.application.commands.bulkApplicationAction;

import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.dtos.BulkActionResult;
import com.iyte_yazilim.proje_pazari.domain.entities.ProjectApplication;
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
public class BulkApplicationActionHandler
        implements IRequestHandler<BulkApplicationActionCommand, ApiResponse<BulkActionResult>> {

    private final ProjectApplicationRepository applicationRepository;
    private final ProjectApplicationMapper applicationMapper;

    @Override
    @Transactional
    public ApiResponse<BulkActionResult> handle(BulkApplicationActionCommand command) {
        BulkActionResult result = new BulkActionResult();

        if (command.applicationIds() == null || command.applicationIds().isEmpty()) {
            return ApiResponse.validationError("Application IDs list cannot be empty");
        }

        for (String appId : command.applicationIds()) {
            try {
                ProjectApplicationEntity appEntity =
                        applicationRepository
                                .findById(appId)
                                .orElseThrow(() -> new ApplicationNotFoundException(appId));

                // Delegate to domain aggregate — enforces PENDING guard
                ProjectApplication application = applicationMapper.entityToDomain(appEntity);

                switch (command.action().toUpperCase()) {
                    case "APPROVE":
                        application.approve();
                        break;
                    case "REJECT":
                        application.reject();
                        break;
                    default:
                        result.addFailure(appId, "Unknown action: " + command.action());
                        continue;
                }

                // Sync domain state back to persistence entity
                appEntity.setStatus(application.getStatus());
                applicationRepository.save(appEntity);
                result.incrementSuccess();
            } catch (Exception e) {
                // Intentional: domain exceptions (e.g. ApplicationNotFoundException,
                // IllegalApplicationStateException) are caught here and recorded as per-item
                // failures rather than propagated. This preserves bulk-operation semantics —
                // a single missing or invalid item must not abort the entire batch.
                // GlobalExceptionHandler will NOT handle these; failures are surfaced
                // in BulkActionResult instead.
                result.addFailure(appId, e.getMessage());
            }
        }

        return ApiResponse.success(result, "Bulk application action completed");
    }
}
