package com.iyte_yazilim.proje_pazari.application.commands.adminDeleteUser;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDeleteUserHandler
        implements IRequestHandler<AdminDeleteUserCommand, ApiResponse<Void>> {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public ApiResponse<Void> handle(AdminDeleteUserCommand command) {
        UserEntity user = userRepository.findById(command.userId()).orElse(null);

        if (user == null) {
            return ApiResponse.notFound("User not found with id: " + command.userId());
        }

        // Soft delete: deactivate the user
        user.setIsActive(false);
        userRepository.save(user);

        return ApiResponse.success(null, "User deleted (deactivated) successfully");
    }
}
