package com.iyte_yazilim.proje_pazari.application.commands.bulkUserAction;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.BulkActionResult;
import java.util.List;

public record BulkUserActionCommand(String action, List<String> userIds)
        implements IRequest<ApiResponse<BulkActionResult>> {}
