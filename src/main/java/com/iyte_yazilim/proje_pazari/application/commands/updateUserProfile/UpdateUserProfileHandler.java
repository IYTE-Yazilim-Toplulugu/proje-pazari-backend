package com.iyte_yazilim.proje_pazari.application.commands.updateUserProfile;

import com.iyte_yazilim.proje_pazari.application.dtos.UserDto;
import com.iyte_yazilim.proje_pazari.application.mappers.UserDtoMapper;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateUserProfileHandler
        implements IRequestHandler<UpdateUserProfileCommand, ApiResponse<UserDto>> {

    private final UserRepository userRepository;
    private final UserDtoMapper userDtoMapper;
    private final MessageService messageService; // EKLENMELI

    @Override
    @Transactional(
            timeoutString = "${spring.transaction.timeout:30}",
            rollbackFor = Exception.class,
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public ApiResponse<UserDto> handle(UpdateUserProfileCommand command) {
        try {
            command.validate();
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        }

        UserEntity user = userRepository.findById(command.userId()).orElse(null);

        if (user == null) {
            return ApiResponse.notFound(
                    messageService.getMessage(
                            "user.not.found.with.id", new Object[] {command.userId()}));
        }

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

        return ApiResponse.success(userDto, messageService.getMessage("user.profile.updated"));
    }
}
