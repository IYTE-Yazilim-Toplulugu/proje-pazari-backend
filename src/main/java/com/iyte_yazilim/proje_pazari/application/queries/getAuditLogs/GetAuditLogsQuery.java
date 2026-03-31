package com.iyte_yazilim.proje_pazari.application.queries.getAuditLogs;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.AuditLogDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedResponse;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;

public record GetAuditLogsQuery(
        int page, int size, String action, String performedBy, String entityType, String entityId)
        implements IRequest<ApiResponse<PagedResponse<AuditLogDTO>>> {}
