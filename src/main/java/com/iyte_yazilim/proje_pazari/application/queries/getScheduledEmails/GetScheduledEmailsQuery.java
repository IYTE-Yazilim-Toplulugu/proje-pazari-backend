package com.iyte_yazilim.proje_pazari.application.queries.getScheduledEmails;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.ScheduledEmailDTO;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import java.util.List;

public record GetScheduledEmailsQuery() implements IRequest<ApiResponse<List<ScheduledEmailDTO>>> {}
