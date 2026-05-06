package com.iyte_yazilim.proje_pazari.application.commands.adminUpdateUser;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;

public record AdminUpdateUserCommand(
        String userId,
        RoleType role,
        Boolean isActive,
        String firstName,
        String lastName,
        String description)
        implements ICommand<ApiResponse<Void>> {}
