package com.iyte_yazilim.proje_pazari.application.commands.adminDeleteUser;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.events.UserDeletedEvent;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
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
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public ApiResponse<Void> handle(AdminDeleteUserCommand command) {
        UserEntity user =
                userRepository
                        .findById(command.userId())
                        .orElseThrow(() -> new UserNotFoundException(command.userId()));

        // Soft delete: deactivate the user
        user.setIsActive(false);
        userRepository.save(user);

        // Publish user deleted event for Elasticsearch indexing
        applicationEventPublisher.publishEvent(new UserDeletedEvent(command.userId()));

        return ApiResponse.success(null, "User deleted (deactivated) successfully");
    }
}
