package com.iyte_yazilim.proje_pazari.application.queries.getActiveSessions;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.ActiveSessionDTO;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import java.util.List;

public record GetActiveSessionsQuery() implements IRequest<ApiResponse<List<ActiveSessionDTO>>> {}
