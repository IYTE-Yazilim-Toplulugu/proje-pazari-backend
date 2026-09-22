package com.iyte_yazilim.proje_pazari.application.commands.adminFeatureProject;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ICommand;

public record AdminFeatureProjectCommand(String projectId, boolean featured)
        implements ICommand<ApiResponse<Void>> {}
