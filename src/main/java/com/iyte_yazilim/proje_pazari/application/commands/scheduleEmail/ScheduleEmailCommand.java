package com.iyte_yazilim.proje_pazari.application.commands.scheduleEmail;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import java.time.LocalDateTime;

public record ScheduleEmailCommand(
        String subject, String body, String targetRole, LocalDateTime scheduledAt)
        implements ICommand<ApiResponse<Void>> {}
