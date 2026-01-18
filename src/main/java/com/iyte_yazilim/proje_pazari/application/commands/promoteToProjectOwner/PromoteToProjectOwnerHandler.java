package com.iyte_yazilim.proje_pazari.application.commands.promoteToProjectOwner;

import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.enums.UserRole;
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
public class PromoteToProjectOwnerHandler
        implements IRequestHandler<PromoteToProjectOwnerCommand, ApiResponse<Void>> {

    private final UserRepository userRepository;
    private final MessageService messageService;

    @Override
    @Transactional(
            timeoutString = "${spring.transaction.timeout:30}",
            rollbackFor = Exception.class,
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public ApiResponse<Void> handle(PromoteToProjectOwnerCommand command) {
        UserEntity user = userRepository.findById(command.userId()).orElse(null);

        if (user == null) {
            return ApiResponse.notFound(
                    messageService.getMessage(
                            "user.not.found.with.id", new Object[] {command.userId()}));
        }

        if (user.getRole() == UserRole.PROJECT_OWNER) {
            return ApiResponse.validationError(
                    messageService.getMessage("user.already.project.owner"));
        }

        if (user.getRole() == UserRole.ADMIN) {
            return ApiResponse.validationError(
                    messageService.getMessage("admin.cannot.be.demoted"));
        }

        user.setRole(UserRole.PROJECT_OWNER);
        userRepository.save(user);

        return ApiResponse.success(null, messageService.getMessage("user.promoted.to.project.owner"));
    }
}
