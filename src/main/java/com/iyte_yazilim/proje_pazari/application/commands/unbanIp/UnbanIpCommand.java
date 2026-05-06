package com.iyte_yazilim.proje_pazari.application.commands.unbanIp;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ICommand;

public record UnbanIpCommand(String ipAddress) implements ICommand<ApiResponse<Void>> {}
