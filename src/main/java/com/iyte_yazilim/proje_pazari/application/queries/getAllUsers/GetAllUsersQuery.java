package com.iyte_yazilim.proje_pazari.application.queries.getAllUsers;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.UserDto;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import java.util.List;

public record GetAllUsersQuery() implements IRequest<ApiResponse<List<UserDto>>> {}
