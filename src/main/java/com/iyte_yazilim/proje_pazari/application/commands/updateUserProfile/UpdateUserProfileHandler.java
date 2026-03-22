package com.iyte_yazilim.proje_pazari.application.commands.updateUserProfile;

import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.dtos.UserDto;
import com.iyte_yazilim.proje_pazari.application.mappers.UserDtoMapper;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.events.UserUpdatedEvent;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateUserProfileHandler
        implements IRequestHandler<UpdateUserProfileCommand, ApiResponse<UserDto>> {

    private final UserRepository userRepository;
    private final UserDtoMapper userDtoMapper;
    private final MessageService messageService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional(
            timeoutString = "${spring.transaction.timeout:30}",
            rollbackFor = Exception.class,
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public ApiResponse<UserDto> handle(UpdateUserProfileCommand command) {
        UserEntity user =
                userRepository
                        .findById(command.userId())
                        .orElseThrow(() -> new UserNotFoundException(command.userId()));

        // Update fields
        if (command.firstName() != null) {
            user.setFirstName(command.firstName());
        }
        if (command.lastName() != null) {
            user.setLastName(command.lastName());
        }
        if (command.description() != null) {
            user.setDescription(command.description());
        }
        if (command.linkedinUrl() != null) {
            user.setLinkedinUrl(command.linkedinUrl().isBlank() ? null : command.linkedinUrl());
        }
        if (command.githubUrl() != null) {
            user.setGithubUrl(command.githubUrl().isBlank() ? null : command.githubUrl());
        }

        UserEntity savedUser = userRepository.save(user);
        UserDto userDto = userDtoMapper.toDto(savedUser);

        applicationEventPublisher.publishEvent(
                new UserUpdatedEvent(
                        savedUser.getId().toString(),
                        savedUser.getEmail(),
                        savedUser.getFirstName(),
                        LocalDateTime.now()));

        return ApiResponse.success(userDto, messageService.getMessage("user.profile.updated"));
    }
}
