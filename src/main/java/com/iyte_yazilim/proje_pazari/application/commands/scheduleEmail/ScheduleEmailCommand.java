package com.iyte_yazilim.proje_pazari.application.commands.scheduleEmail;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import java.time.LocalDateTime;

public record ScheduleEmailCommand(
        String subject, String body, String targetRole, LocalDateTime scheduledAt)
        implements IRequest<ApiResponse<Void>> {}
