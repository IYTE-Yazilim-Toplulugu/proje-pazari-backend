package com.iyte_yazilim.proje_pazari.application.commands.adminDeleteUser;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;

public record AdminDeleteUserCommand(String userId) implements IRequest<ApiResponse<Void>> {}
