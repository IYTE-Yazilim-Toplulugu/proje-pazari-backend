package com.iyte_yazilim.proje_pazari.application.commands.cancelScheduledEmail;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ScheduledEmailRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ScheduledEmailEntity;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CancelScheduledEmailHandler
        implements IRequestHandler<CancelScheduledEmailCommand, ApiResponse<Void>> {

    private final ScheduledEmailRepository scheduledEmailRepository;

    @Override
    public ApiResponse<Void> handle(CancelScheduledEmailCommand command) {
        if (command.emailId() == null || command.emailId().isBlank()) {
            return ApiResponse.validationError("Email ID is required");
        }

        Optional<ScheduledEmailEntity> optEntity =
                scheduledEmailRepository.findById(command.emailId());
        if (optEntity.isEmpty()) {
            return ApiResponse.notFound("Scheduled email not found: " + command.emailId());
        }

        ScheduledEmailEntity entity = optEntity.get();
        if (!"PENDING".equals(entity.getStatus())) {
            return ApiResponse.validationError(
                    "Only PENDING emails can be cancelled. Current status: " + entity.getStatus());
        }

        entity.setStatus("CANCELLED");
        scheduledEmailRepository.save(entity);
        return ApiResponse.success(null, "Scheduled email cancelled");
    }
}
