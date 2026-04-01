package com.iyte_yazilim.proje_pazari.application.commands.adminDeleteUser;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;

public record AdminDeleteUserCommand(String userId) implements IRequest<ApiResponse<Void>> {}
