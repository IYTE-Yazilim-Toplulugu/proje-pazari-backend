package com.iyte_yazilim.proje_pazari.application.commands.scheduleEmail;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ScheduledEmailRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ScheduledEmailEntity;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleEmailHandler
        implements IRequestHandler<ScheduleEmailCommand, ApiResponse<Void>> {

    private final ScheduledEmailRepository scheduledEmailRepository;

    @Override
    public ApiResponse<Void> handle(ScheduleEmailCommand command) {
        if (command.subject() == null || command.subject().isBlank()) {
            return ApiResponse.validationError("Email subject is required");
        }
        if (command.body() == null || command.body().isBlank()) {
            return ApiResponse.validationError("Email body is required");
        }
        if (command.scheduledAt() == null) {
            return ApiResponse.validationError("Scheduled time is required");
        }
        if (command.scheduledAt().isBefore(LocalDateTime.now())) {
            return ApiResponse.validationError("Scheduled time must be in the future");
        }

        String currentUser = "SYSTEM";
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                currentUser = auth.getName();
            }
        } catch (Exception ignored) {
        }

        ScheduledEmailEntity entity =
                ScheduledEmailEntity.builder()
                        .subject(command.subject())
                        .body(command.body())
                        .targetRole(command.targetRole() != null ? command.targetRole() : "ALL")
                        .scheduledAt(command.scheduledAt())
                        .status("PENDING")
                        .createdBy(currentUser)
                        .build();

        scheduledEmailRepository.save(entity);
        return ApiResponse.success(null, "Email scheduled for " + command.scheduledAt());
    }
}
