package com.iyte_yazilim.proje_pazari.application.commands.bulkApplicationAction;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.application.dtos.BulkActionResult;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import java.util.List;

public record BulkApplicationActionCommand(String action, List<String> applicationIds)
        implements ICommand<ApiResponse<BulkActionResult>> {}
