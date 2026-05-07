package com.iyte_yazilim.proje_pazari.application.queries.adminGetUser;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.dtos.UserAdminDTO;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminGetUserHandler
        implements IRequestHandler<AdminGetUserQuery, ApiResponse<UserAdminDTO>> {

    private final UserRepository userRepository;

    @Override
    public ApiResponse<UserAdminDTO> handle(AdminGetUserQuery query) {
        UserEntity user = userRepository.findById(query.userId()).orElse(null);

        if (user == null) {
            return ApiResponse.notFound("User not found with id: " + query.userId());
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

        return ApiResponse.success(dto, "User details retrieved successfully");
    }
}
