package com.iyte_yazilim.proje_pazari.application.commands.sendTargetedEmail;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.dtos.EmailDto;
import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendTargetedEmailHandler
        implements IRequestHandler<SendTargetedEmailCommand, ApiResponse<Void>> {

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    public ApiResponse<Void> handle(SendTargetedEmailCommand command) {
        if (command.userIds() == null || command.userIds().isEmpty()) {
            return ApiResponse.validationError("User IDs list cannot be empty");
        }
        if (command.subject() == null || command.subject().isBlank()) {
            return ApiResponse.validationError("Email subject is required");
        }
        if (command.body() == null || command.body().isBlank()) {
            return ApiResponse.validationError("Email body is required");
        }

        int sentCount = 0;
        int failedCount = 0;

        for (String userId : command.userIds()) {
            Optional<UserEntity> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                try {
                    UserEntity user = userOpt.get();
                    emailService.sendEmailAsync(
                            new EmailDto(user.getEmail(), command.subject(), command.body()));
                    sentCount++;
                } catch (Exception e) {
                    log.error("Failed to send targeted email to userId: {}", userId, e);
                    failedCount++;
                }
            } else {
                failedCount++;
                log.warn("User not found for targeted email: {}", userId);
            }
        }

        return ApiResponse.success(
                null, "Targeted email queued: " + sentCount + " sent, " + failedCount + " failed");
    }
}
