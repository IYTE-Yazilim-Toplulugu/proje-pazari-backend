package com.iyte_yazilim.proje_pazari.application.commands.deactivateAccount;

import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeactivateAccountHandler
        implements IRequestHandler<DeactivateAccountCommand, ApiResponse<Void>> {

    private final UserRepository userRepository;
    private final MessageService messageService;

    @Override
    @Transactional(
            timeoutString = "${spring.transaction.timeout:30}",
            rollbackFor = Exception.class,
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public ApiResponse<Void> handle(DeactivateAccountCommand command) {
        UserEntity user =
                userRepository
                        .findById(command.userId())
                        .orElseThrow(() -> new UserNotFoundException(command.userId()));

        // Log deactivation reason
        if (command.reason() != null && !command.reason().isBlank()) {
            log.info("User {} deactivated account. Reason: {}", command.userId(), command.reason());
        }

        // Soft-delete the user by marking the account as inactive
        // This preserves referential integrity with related entities (e.g., projects, applications)
        user.setIsActive(false);
        userRepository.save(user);

        return ApiResponse.success(null, messageService.getMessage("user.account.deactivated"));
    }
}
