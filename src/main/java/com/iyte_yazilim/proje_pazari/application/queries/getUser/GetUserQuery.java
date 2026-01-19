package com.iyte_yazilim.proje_pazari.application.queries.getUser;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.UserDto;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;

public record GetUserQuery(String userId) implements IRequest<ApiResponse<UserDto>> {}
