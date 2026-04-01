package com.iyte_yazilim.proje_pazari.application.queries.getFlaggedContent;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.dtos.FlaggedContentDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedResponse;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.FlaggedContentRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.FlaggedContentEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetFlaggedContentHandler
        implements IRequestHandler<
                GetFlaggedContentQuery, ApiResponse<PagedResponse<FlaggedContentDTO>>> {

    private final FlaggedContentRepository flaggedContentRepository;

    @Override
    public ApiResponse<PagedResponse<FlaggedContentDTO>> handle(GetFlaggedContentQuery query) {
        PageRequest pageRequest =
                PageRequest.of(
                        query.page(), query.size(), Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<FlaggedContentEntity> flagPage =
                flaggedContentRepository.findWithFilters(
                        query.status(), query.contentType(), pageRequest);

        List<FlaggedContentDTO> flags = flagPage.getContent().stream().map(this::mapToDTO).toList();

        PagedResponse<FlaggedContentDTO> pagedResponse =
                PagedResponse.<FlaggedContentDTO>builder()
                        .content(flags)
                        .page(flagPage.getNumber())
                        .size(flagPage.getSize())
                        .totalElements(flagPage.getTotalElements())
                        .totalPages(flagPage.getTotalPages())
                        .build();

        return ApiResponse.success(pagedResponse, "Flagged content retrieved successfully");
    }

    private FlaggedContentDTO mapToDTO(FlaggedContentEntity entity) {
        return new FlaggedContentDTO(
                entity.getId(),
                entity.getContentType(),
                entity.getContentId(),
                entity.getReason(),
                entity.getReportedBy(),
                entity.getStatus(),
                entity.getReviewedBy(),
                entity.getReviewNote(),
                entity.getCreatedAt(),
                entity.getReviewedAt());
    }
}
