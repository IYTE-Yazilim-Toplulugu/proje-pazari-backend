package com.iyte_yazilim.proje_pazari.application.commands.withdrawApplication;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Command to withdraw a submitted application")
public record WithdrawApplicationCommand(
        @Schema(hidden = true) String applicationId, @Schema(hidden = true) String userId)
        implements ICommand<ApiResponse<Void>> {}
