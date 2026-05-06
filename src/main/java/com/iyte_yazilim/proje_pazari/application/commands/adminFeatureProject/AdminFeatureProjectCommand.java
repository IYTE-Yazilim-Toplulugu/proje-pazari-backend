package com.iyte_yazilim.proje_pazari.application.commands.adminFeatureProject;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;

public record AdminFeatureProjectCommand(String projectId, boolean featured)
        implements ICommand<ApiResponse<Void>> {}
