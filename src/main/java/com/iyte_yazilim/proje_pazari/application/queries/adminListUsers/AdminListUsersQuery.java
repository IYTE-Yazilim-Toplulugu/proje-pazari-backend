package com.iyte_yazilim.proje_pazari.application.queries.adminListUsers;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedResponse;
import com.iyte_yazilim.proje_pazari.application.dtos.UserAdminDTO;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;

public record AdminListUsersQuery(
        int page, int size, RoleType role, Boolean isActive, String search)
        implements IRequest<ApiResponse<PagedResponse<UserAdminDTO>>> {}
