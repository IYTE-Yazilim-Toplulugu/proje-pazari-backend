package com.iyte_yazilim.proje_pazari.application.commands.adminUpdateUser;

import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUpdateUserHandler
        implements IRequestHandler<AdminUpdateUserCommand, ApiResponse<Void>> {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public ApiResponse<Void> handle(AdminUpdateUserCommand command) {
        UserEntity user =
                userRepository
                        .findById(command.userId())
                        .orElseThrow(() -> new UserNotFoundException(command.userId()));

        if (command.role() != null) {
            user.getRoles().clear();
            user.getRoles().add(command.role());
        }
        if (command.isActive() != null) {
            user.setIsActive(command.isActive());
        }
        if (command.firstName() != null) {
            user.setFirstName(command.firstName());
        }
        if (command.lastName() != null) {
            user.setLastName(command.lastName());
        }
        if (command.description() != null) {
            user.setDescription(command.description());
        }

        userRepository.save(user);
        return ApiResponse.success(null, "User updated successfully");
    }
}
