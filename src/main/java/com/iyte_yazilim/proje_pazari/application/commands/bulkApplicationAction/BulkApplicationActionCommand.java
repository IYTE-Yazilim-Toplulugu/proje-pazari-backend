package com.iyte_yazilim.proje_pazari.application.commands.bulkApplicationAction;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.BulkActionResult;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import java.util.List;

public record BulkApplicationActionCommand(String action, List<String> applicationIds)
        implements IRequest<ApiResponse<BulkActionResult>> {}
