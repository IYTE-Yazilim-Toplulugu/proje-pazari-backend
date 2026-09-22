package com.iyte_yazilim.proje_pazari.application.queries.getActiveSessions;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.ActiveSessionDTO;
import java.util.List;

public record GetActiveSessionsQuery() implements IRequest<ApiResponse<List<ActiveSessionDTO>>> {}
