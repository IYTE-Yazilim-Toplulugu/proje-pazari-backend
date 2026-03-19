package com.iyte_yazilim.proje_pazari.application.commands.reviewFlaggedContent;

import com.iyte_yazilim.proje_pazari.domain.exceptions.FlaggedContentNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.FlaggedContentRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.FlaggedContentEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewFlaggedContentHandler
        implements IRequestHandler<ReviewFlaggedContentCommand, ApiResponse<Void>> {

    private final FlaggedContentRepository flaggedContentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ApiResponse<Void> handle(ReviewFlaggedContentCommand command) {
        FlaggedContentEntity flag =
                flaggedContentRepository
                        .findById(command.flagId())
                        .orElseThrow(() -> new FlaggedContentNotFoundException(command.flagId()));

        String action = command.action() != null ? command.action().toUpperCase() : "";

        String reviewedBy = "SYSTEM";
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) {
                reviewedBy = auth.getName();
            }
        } catch (Exception ignored) {
        }

        switch (action) {
            case "APPROVE":
                flag.setStatus("APPROVED");
                break;
            case "REMOVE":
                flag.setStatus("REMOVED");
                break;
            case "BAN_USER":
                flag.setStatus("REMOVED");
                if ("USER".equals(flag.getContentType())) {
                    UserEntity user =
                            userRepository
                                    .findById(flag.getContentId())
                                    .orElseThrow(
                                            () -> new UserNotFoundException(flag.getContentId()));
                    user.setIsActive(false);
                    userRepository.save(user);
                }
                break;
            default:
                return ApiResponse.validationError(
                        "Invalid action. Must be APPROVE, REMOVE, or BAN_USER");
        }

        flag.setReviewedBy(reviewedBy);
        flag.setReviewNote(command.reviewNote());
        flag.setReviewedAt(LocalDateTime.now());
        flaggedContentRepository.save(flag);

        return ApiResponse.success(null, "Flagged content reviewed successfully");
    }
}
