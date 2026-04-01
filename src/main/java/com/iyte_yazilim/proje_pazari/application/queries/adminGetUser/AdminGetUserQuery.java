package com.iyte_yazilim.proje_pazari.application.queries.adminGetUser;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.UserAdminDTO;

public record AdminGetUserQuery(String userId) implements IRequest<ApiResponse<UserAdminDTO>> {}
