package com.iyte_yazilim.proje_pazari.application.queries.adminGetUser;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.UserAdminDTO;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;

public record AdminGetUserQuery(String userId) implements IRequest<ApiResponse<UserAdminDTO>> {}
