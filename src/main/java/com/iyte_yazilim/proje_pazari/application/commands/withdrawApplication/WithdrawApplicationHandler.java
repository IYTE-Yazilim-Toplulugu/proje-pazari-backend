package com.iyte_yazilim.proje_pazari.application.commands.withdrawApplication;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class WithdrawApplicationHandler
        implements IRequestHandler<WithdrawApplicationCommand, ApiResponse<Void>> {

    private final ProjectApplicationRepository applicationRepository;
    private final MessageService messageService;

    @Override
    @Transactional(
            timeoutString = "${spring.transaction.timeout:30}",
            rollbackFor = Exception.class,
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public ApiResponse<Void> handle(WithdrawApplicationCommand command) {

        // --- 1. Verify Application Exists ---
        ProjectApplicationEntity applicationEntity =
                applicationRepository.findById(command.applicationId()).orElse(null);
        if (applicationEntity == null) {
            return ApiResponse.notFound(
                    messageService.getMessage(
                            "application.not.found", new Object[] {command.applicationId()}));
        }

        // --- 2. Verify the applicant owns this application ---
        if (!applicationEntity.getUser().getId().equals(command.userId())) {
            return ApiResponse.forbidden(
                    messageService.getMessage("application.withdraw.forbidden"));
        }

        // --- 3. Only PENDING applications can be withdrawn ---
        if (applicationEntity.getStatus() != ApplicationStatus.PENDING) {
            return ApiResponse.badRequest(
                    messageService.getMessage("application.withdraw.not.pending"));
        }

        // --- 4. Withdraw (soft-delete via status transition) ---
        applicationEntity.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(applicationEntity);

        // --- 5. Response ---
        return ApiResponse.success(
                null, messageService.getMessage("application.withdrawn.success"));
    }
}
