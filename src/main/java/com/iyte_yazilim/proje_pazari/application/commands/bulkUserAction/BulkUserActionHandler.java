package com.iyte_yazilim.proje_pazari.application.commands.bulkUserAction;

import com.iyte_yazilim.proje_pazari.application.dtos.BulkActionResult;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BulkUserActionHandler
        implements IRequestHandler<BulkUserActionCommand, ApiResponse<BulkActionResult>> {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public ApiResponse<BulkActionResult> handle(BulkUserActionCommand command) {
        BulkActionResult result = new BulkActionResult();

        if (command.userIds() == null || command.userIds().isEmpty()) {
            return ApiResponse.validationError("User IDs list cannot be empty");
        }

        for (String userId : command.userIds()) {
            try {
                UserEntity user =
                        userRepository
                                .findById(userId)
                                .orElseThrow(() -> new UserNotFoundException(userId));

                switch (command.action().toUpperCase()) {
                    case "DELETE":
                    case "SUSPEND":
                        user.setIsActive(false);
                        userRepository.save(user);
                        result.incrementSuccess();
                        break;
                    case "ACTIVATE":
                        user.setIsActive(true);
                        userRepository.save(user);
                        result.incrementSuccess();
                        break;
                    case "PROMOTE_TO_ADMIN":
                        user.getRoles().clear();
                        user.getRoles().add(RoleType.ADMIN);
                        userRepository.save(user);
                        result.incrementSuccess();
                        break;
                    case "DEMOTE_TO_USER":
                        user.getRoles().clear();
                        user.getRoles().add(RoleType.USER);
                        userRepository.save(user);
                        result.incrementSuccess();
                        break;
                    default:
                        result.addFailure(userId, "Unknown action: " + command.action());
                }
            } catch (Exception e) {
                // Intentional: domain exceptions (e.g. UserNotFoundException) are caught here
                // and recorded as per-item failures rather than propagated. This preserves
                // bulk-operation semantics — a single missing or invalid item must not abort the
                // entire batch. GlobalExceptionHandler will NOT handle these; failures are surfaced
                // in BulkActionResult instead.
                result.addFailure(userId, e.getMessage());
            }
        }

        return ApiResponse.success(result, "Bulk user action completed");
    }
}
