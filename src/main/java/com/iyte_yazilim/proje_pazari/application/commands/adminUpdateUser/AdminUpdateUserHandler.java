package com.iyte_yazilim.proje_pazari.application.commands.adminUpdateUser;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.UserMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUpdateUserHandler
        implements IRequestHandler<AdminUpdateUserCommand, ApiResponse<Void>> {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public ApiResponse<Void> handle(AdminUpdateUserCommand command) {
        UserEntity userEntity =
                userRepository
                        .findById(command.userId())
                        .orElseThrow(() -> new UserNotFoundException(command.userId()));

        // Map to domain aggregate for guarded mutations
        User user = userMapper.entityToDomain(userEntity);

        if (command.role() != null) {
            user.assignRole(command.role());
        }
        if (command.isActive() != null) {
            if (command.isActive()) {
                // Only activate if currently inactive (guard handles idempotency error)
                if (!user.isActive()) {
                    user.activate();
                }
            } else {
                // Only deactivate if currently active (guard handles idempotency error)
                if (user.isActive()) {
                    user.deactivate();
                }
            }
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

        // Apply domain state back to persistence entity
        userMapper.applyDomainToEntity(user, userEntity);
        userRepository.save(userEntity);
        return ApiResponse.success(null, "User updated successfully");
    }
}
