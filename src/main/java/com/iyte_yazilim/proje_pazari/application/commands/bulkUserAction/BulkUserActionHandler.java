package com.iyte_yazilim.proje_pazari.application.commands.bulkUserAction;

import com.iyte_yazilim.proje_pazari.application.dtos.BulkActionResult;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
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
                UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException(userId));

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
                    case "CHANGE_ROLE_TO_PROJECT_OWNER":
                        user.setRole(RoleType.PROJECT_OWNER);
                        userRepository.save(user);
                        result.incrementSuccess();
                        break;
                    default:
                        result.addFailure(userId, "Unknown action: " + command.action());
                }
            } catch (Exception e) {
                result.addFailure(userId, e.getMessage());
            }
        }

        return ApiResponse.success(result, "Bulk user action completed");
    }
}
