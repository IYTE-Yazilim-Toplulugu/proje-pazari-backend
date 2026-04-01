package com.iyte_yazilim.proje_pazari.application.commands.adminUpdateUser;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;

public record AdminUpdateUserCommand(
        String userId,
        RoleType role,
        Boolean isActive,
        String firstName,
        String lastName,
        String description)
        implements IRequest<ApiResponse<Void>> {}
