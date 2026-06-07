package com.iyte_yazilim.proje_pazari.application.queries.adminGetUser;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.dtos.UserAdminDTO;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminGetUserHandler
        implements IRequestHandler<AdminGetUserQuery, ApiResponse<UserAdminDTO>> {

    private final UserRepository userRepository;
    private final MessageService messageService;

    @Override
    public ApiResponse<UserAdminDTO> handle(AdminGetUserQuery query) {
        UserEntity user = userRepository.findById(query.userId()).orElse(null);

        if (user == null) {
            throw new UserNotFoundException(query.userId());
        }

        UserAdminDTO dto =
                new UserAdminDTO(
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getDescription(),
                        user.getProfilePictureUrl(),
                        user.getLinkedinUrl(),
                        user.getGithubUrl(),
                        user.getRoles(),
                        user.getIsActive(),
                        user.getCreatedAt(),
                        user.getUpdatedAt(),
                        userRepository.countProjectsByUserId(user.getId()),
                        userRepository.countApplicationsByUserId(user.getId()));

        return ApiResponse.success(dto, messageService.getMessage("user.retrieved.success"));
    }
}
