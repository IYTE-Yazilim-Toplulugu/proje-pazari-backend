package com.iyte_yazilim.proje_pazari.application.commands.bulkUserAction;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.application.dtos.BulkActionResult;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import java.util.List;

public record BulkUserActionCommand(String action, List<String> userIds)
        implements ICommand<ApiResponse<BulkActionResult>> {}
