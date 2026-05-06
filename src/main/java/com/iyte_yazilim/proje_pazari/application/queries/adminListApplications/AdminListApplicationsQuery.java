package com.iyte_yazilim.proje_pazari.application.queries.adminListApplications;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.ApplicationAdminDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedResponse;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;

public record AdminListApplicationsQuery(
        int page, int size, ApplicationStatus status, String projectId, String userId)
        implements IRequest<ApiResponse<PagedResponse<ApplicationAdminDTO>>> {}
