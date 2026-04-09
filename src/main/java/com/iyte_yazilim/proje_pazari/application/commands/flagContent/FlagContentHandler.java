package com.iyte_yazilim.proje_pazari.application.commands.flagContent;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.FlaggedContentRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.FlaggedContentEntity;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FlagContentHandler implements IRequestHandler<FlagContentCommand, ApiResponse<Void>> {

    private final FlaggedContentRepository flaggedContentRepository;

    @Override
    @Transactional
    public ApiResponse<Void> handle(FlagContentCommand command) {
        if (command.contentType() == null || command.contentId() == null) {
            return ApiResponse.validationError("Content type and content ID are required");
        }

        String validType = command.contentType().toUpperCase();
        if (!validType.equals("USER")
                && !validType.equals("PROJECT")
                && !validType.equals("APPLICATION")) {
            return ApiResponse.validationError(
                    "Invalid content type. Must be USER, PROJECT, or APPLICATION");
        }

        String validReason =
                command.reason() != null ? command.reason().toUpperCase() : "INAPPROPRIATE";
        if (!validReason.equals("SPAM")
                && !validReason.equals("INAPPROPRIATE")
                && !validReason.equals("OFFENSIVE")) {
            return ApiResponse.validationError(
                    "Invalid reason. Must be SPAM, INAPPROPRIATE, or OFFENSIVE");
        }

        String reportedBy = "SYSTEM";
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) {
                reportedBy = auth.getName();
            }
        } catch (Exception ignored) {
        }

        FlaggedContentEntity entity =
                FlaggedContentEntity.builder()
                        .contentType(validType)
                        .contentId(command.contentId())
                        .reason(validReason)
                        .reportedBy(reportedBy)
                        .build();

        flaggedContentRepository.save(entity);

        return ApiResponse.success(null, "Content flagged successfully");
    }
}
