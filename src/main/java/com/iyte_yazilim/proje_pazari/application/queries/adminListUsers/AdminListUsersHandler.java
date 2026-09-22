package com.iyte_yazilim.proje_pazari.application.queries.adminListUsers;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedResponse;
import com.iyte_yazilim.proje_pazari.application.dtos.UserAdminDTO;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminListUsersHandler
        implements IRequestHandler<AdminListUsersQuery, ApiResponse<PagedResponse<UserAdminDTO>>> {

    private final UserRepository userRepository;

    @Override
    public ApiResponse<PagedResponse<UserAdminDTO>> handle(AdminListUsersQuery query) {
        PageRequest pageRequest =
                PageRequest.of(
                        query.page(), query.size(), Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Object[]> rowPage =
                userRepository.findWithFiltersAndCounts(
                        query.role(), query.isActive(), query.search(), pageRequest);

        List<UserAdminDTO> users = rowPage.getContent().stream().map(this::mapFromRow).toList();

        PagedResponse<UserAdminDTO> pagedResponse =
                PagedResponse.<UserAdminDTO>builder()
                        .content(users)
                        .page(rowPage.getNumber())
                        .size(rowPage.getSize())
                        .totalElements(rowPage.getTotalElements())
                        .totalPages(rowPage.getTotalPages())
                        .build();

        return ApiResponse.success(pagedResponse, "Users retrieved successfully");
    }

    private UserAdminDTO mapFromRow(Object[] row) {
        UserEntity entity = (UserEntity) row[0];
        int projectCount = ((Number) row[1]).intValue();
        int applicationCount = ((Number) row[2]).intValue();
        return new UserAdminDTO(
                entity.getId(),
                entity.getEmail(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getDescription(),
                entity.getProfilePictureUrl(),
                entity.getLinkedinUrl(),
                entity.getGithubUrl(),
                entity.getRoles(),
                entity.getIsActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                projectCount,
                applicationCount);
    }
}
