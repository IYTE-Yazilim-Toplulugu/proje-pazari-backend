package com.iyte_yazilim.proje_pazari.application.commands.sendTargetedEmail;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import java.util.List;

public record SendTargetedEmailCommand(List<String> userIds, String subject, String body)
        implements ICommand<ApiResponse<Void>> {}
