package com.iyte_yazilim.proje_pazari.application.commands.bulkApplicationAction;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.BulkActionResult;
import java.util.List;

public record BulkApplicationActionCommand(String action, List<String> applicationIds)
        implements IRequest<ApiResponse<BulkActionResult>> {}
