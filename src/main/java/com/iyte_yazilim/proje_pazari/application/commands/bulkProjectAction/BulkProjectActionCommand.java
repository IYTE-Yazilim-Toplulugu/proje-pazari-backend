package com.iyte_yazilim.proje_pazari.application.commands.bulkProjectAction;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.BulkActionResult;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import java.util.List;

public record BulkProjectActionCommand(String action, List<String> projectIds)
        implements IRequest<ApiResponse<BulkActionResult>> {}
