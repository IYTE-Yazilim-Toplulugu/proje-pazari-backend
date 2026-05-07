package com.iyte_yazilim.proje_pazari.application.commands.adminDeleteUser;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.events.UserDeactivatedEvent;
import com.iyte_yazilim.proje_pazari.domain.events.UserDeletedEvent;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.UserMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDeleteUserHandler
        implements IRequestHandler<AdminDeleteUserCommand, ApiResponse<Void>> {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public ApiResponse<Void> handle(AdminDeleteUserCommand command) {
        UserEntity userEntity =
                userRepository
                        .findById(command.userId())
                        .orElseThrow(() -> new UserNotFoundException(command.userId()));

        // Delegate to domain aggregate — enforces lifecycle guard
        User user = userMapper.entityToDomain(userEntity);
        user.deactivate();
        userMapper.applyDomainToEntity(user, userEntity);
        userRepository.save(userEntity);

        // ES sync
        applicationEventPublisher.publishEvent(new UserDeletedEvent(command.userId()));
        // Session revocation
        applicationEventPublisher.publishEvent(
                new UserDeactivatedEvent(command.userId(), userEntity.getEmail()));

        return ApiResponse.success(null, "User deleted (deactivated) successfully");
    }
}
