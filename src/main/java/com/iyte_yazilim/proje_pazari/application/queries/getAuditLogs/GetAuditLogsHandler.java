package com.iyte_yazilim.proje_pazari.application.queries.getAuditLogs;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.dtos.AuditLogDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.AuditLogRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.AuditLogEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetAuditLogsHandler
        implements IRequestHandler<GetAuditLogsQuery, ApiResponse<PagedResponse<AuditLogDTO>>> {

    private final AuditLogRepository auditLogRepository;

    @Override
    public ApiResponse<PagedResponse<AuditLogDTO>> handle(GetAuditLogsQuery query) {
        PageRequest pageRequest =
                PageRequest.of(
                        query.page(), query.size(), Sort.by(Sort.Direction.DESC, "timestamp"));

        Page<AuditLogEntity> logPage =
                auditLogRepository.findWithFilters(
                        query.action(),
                        query.performedBy(),
                        query.entityType(),
                        query.entityId(),
                        pageRequest);

        List<AuditLogDTO> logs = logPage.getContent().stream().map(this::mapToDTO).toList();

        PagedResponse<AuditLogDTO> pagedResponse =
                PagedResponse.<AuditLogDTO>builder()
                        .content(logs)
                        .page(logPage.getNumber())
                        .size(logPage.getSize())
                        .totalElements(logPage.getTotalElements())
                        .totalPages(logPage.getTotalPages())
                        .build();

        return ApiResponse.success(pagedResponse, "Audit logs retrieved successfully");
    }

    private AuditLogDTO mapToDTO(AuditLogEntity entity) {
        return new AuditLogDTO(
                entity.getId(),
                entity.getAction(),
                entity.getEntityType(),
                entity.getEntityId(),
                entity.getPerformedBy(),
                entity.getIpAddress(),
                entity.getTimestamp(),
                entity.getDetails(),
                entity.getStatus());
    }
}
