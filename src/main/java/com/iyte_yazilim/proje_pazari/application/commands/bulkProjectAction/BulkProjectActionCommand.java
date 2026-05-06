package com.iyte_yazilim.proje_pazari.application.commands.bulkProjectAction;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.application.dtos.BulkActionResult;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import java.util.List;

public record BulkProjectActionCommand(String action, List<String> projectIds)
        implements ICommand<ApiResponse<BulkActionResult>> {}
