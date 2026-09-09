package com.iyte_yazilim.proje_pazari.application.commands.deactivateAccount;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.events.UserDeactivatedEvent;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.UserMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
    private final UserMapper userMapper;
    private final MessageService messageService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional(
            timeoutString = "${spring.transaction.timeout:30}",
            rollbackFor = Exception.class,
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public ApiResponse<Void> handle(DeactivateAccountCommand command) {
        UserEntity userEntity =
                userRepository
                        .findById(command.userId())
                        .orElseThrow(() -> new UserNotFoundException(command.userId()));

        if (command.reason() != null && !command.reason().isBlank()) {
            log.info("User {} deactivated account. Reason: {}", command.userId(), command.reason());
        }

        User user = userMapper.entityToDomain(userEntity);
        user.deactivate();
        userMapper.applyDomainToEntity(user, userEntity);
        userRepository.save(userEntity);

        applicationEventPublisher.publishEvent(
                new UserDeactivatedEvent(command.userId(), userEntity.getEmail()));

        return ApiResponse.success(null, messageService.getMessage("user.account.deactivated"));
    }
}
