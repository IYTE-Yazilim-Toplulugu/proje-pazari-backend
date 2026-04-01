package com.iyte_yazilim.proje_pazari.application.commands.adminFeatureProject;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;

public record AdminFeatureProjectCommand(String projectId, boolean featured)
        implements IRequest<ApiResponse<Void>> {}
