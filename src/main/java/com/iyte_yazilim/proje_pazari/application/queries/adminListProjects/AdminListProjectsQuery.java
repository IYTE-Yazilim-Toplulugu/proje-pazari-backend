package com.iyte_yazilim.proje_pazari.application.queries.adminListProjects;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedResponse;
import com.iyte_yazilim.proje_pazari.application.dtos.ProjectAdminDTO;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;

public record AdminListProjectsQuery(
        int page, int size, ProjectStatus status, String ownerId, String search)
        implements IRequest<ApiResponse<PagedResponse<ProjectAdminDTO>>> {}
