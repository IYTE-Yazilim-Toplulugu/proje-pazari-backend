package com.iyte_yazilim.proje_pazari.application.queries.getScheduledEmails;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.ScheduledEmailDTO;
import java.util.List;

public record GetScheduledEmailsQuery() implements IRequest<ApiResponse<List<ScheduledEmailDTO>>> {}
