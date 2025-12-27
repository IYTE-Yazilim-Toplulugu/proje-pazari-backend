package com.iyte_yazilim.proje_pazari.application.queries.getUserProfile;

import com.iyte_yazilim.proje_pazari.application.dtos.ProjectSummaryDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.UserProfileDTO;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetUserProfileHandler implements IRequestHandler<GetUserProfileQuery, ApiResponse<UserProfileDTO>> {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    @Override
    public ApiResponse<UserProfileDTO> handle(GetUserProfileQuery query) {
        UserEntity user = userRepository.findById(query.userId()).orElse(null);

        if (user == null) {
            return ApiResponse.notFound("User with ID " + query.userId() + " not found");
        }

        // Get user statistics
        int projectsCreated = userRepository.countProjectsByUserId(query.userId());
        int applicationsSubmitted = userRepository.countApplicationsByUserId(query.userId());

        // Get user's projects
        List<ProjectEntity> projectEntities = projectRepository.findAll().stream()
                .filter(p -> p.getOwner() != null && p.getOwner().getId().equals(query.userId()))
                .toList();

        List<ProjectSummaryDTO> projects = projectEntities.stream()
                .map(p -> new ProjectSummaryDTO(
                        p.getId(),
                        p.getTitle(),
                        p.getDescription(),
                        p.getStatus().name(),
                        p.getCreatedAt()
                ))
                .collect(Collectors.toList());

        UserProfileDTO profile = new UserProfileDTO(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                (user.getFirstName() != null && user.getLastName() != null) 
                    ? user.getFirstName() + " " + user.getLastName() 
                    : null,
                user.getDescription(),
                user.getProfilePictureUrl(),
                user.getLinkedinUrl(),
                user.getGithubUrl(),
                user.getCreatedAt(),
                projectsCreated,
                applicationsSubmitted,
                projects
        );

        return ApiResponse.success(profile, "User profile retrieved successfully");
    }
}
