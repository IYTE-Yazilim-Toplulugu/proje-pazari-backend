package com.iyte_yazilim.proje_pazari.application.commands.changePassword;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ChangePasswordHandler
        implements IRequestHandler<ChangePasswordCommand, ApiResponse<Void>> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageService messageService;

    @Override
    @Transactional(
            timeoutString = "${spring.transaction.timeout:30}",
            rollbackFor = Exception.class,
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public ApiResponse<Void> handle(ChangePasswordCommand command) {
        UserEntity user =
                userRepository
                        .findById(command.userId())
                        .orElseThrow(() -> new UserNotFoundException(command.userId()));

        if (!passwordEncoder.matches(command.currentPassword(), user.getPassword())) {
            return ApiResponse.validationError(
                    messageService.getMessage("user.password.current.incorrect"));
        }

        user.setPassword(passwordEncoder.encode(command.newPassword()));
        userRepository.save(user);

        return ApiResponse.success(null, messageService.getMessage("user.password.changed"));
    }
}
