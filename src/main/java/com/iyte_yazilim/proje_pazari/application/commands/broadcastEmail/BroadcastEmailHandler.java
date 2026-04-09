package com.iyte_yazilim.proje_pazari.application.commands.broadcastEmail;

import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.dtos.EmailDto;
import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BroadcastEmailHandler
        implements IRequestHandler<BroadcastEmailCommand, ApiResponse<Void>> {

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    public ApiResponse<Void> handle(BroadcastEmailCommand command) {
        if (command.subject() == null || command.subject().isBlank()) {
            return ApiResponse.validationError("Email subject is required");
        }
        if (command.body() == null || command.body().isBlank()) {
            return ApiResponse.validationError("Email body is required");
        }

        List<UserEntity> users;
        if (command.targetRole() != null && !command.targetRole().equalsIgnoreCase("ALL")) {
            try {
                RoleType role = RoleType.valueOf(command.targetRole().toUpperCase());
                users =
                        userRepository
                                .findByRole(
                                        role, org.springframework.data.domain.Pageable.unpaged())
                                .getContent();
            } catch (IllegalArgumentException e) {
                return ApiResponse.validationError("Invalid target role: " + command.targetRole());
            }
        } else {
            users = userRepository.findAll();
        }

        int sentCount = 0;
        for (UserEntity user : users) {
            if (user.getIsActive() != null && user.getIsActive()) {
                try {
                    emailService.sendEmailAsync(
                            new EmailDto(user.getEmail(), command.subject(), command.body()));
                    sentCount++;
                } catch (Exception e) {
                    log.error("Failed to send broadcast email to: {}", user.getEmail(), e);
                }
            }
        }

        return ApiResponse.success(null, "Broadcast email queued for " + sentCount + " recipients");
    }
}
