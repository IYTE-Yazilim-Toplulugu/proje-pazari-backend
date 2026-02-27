package com.iyte_yazilim.proje_pazari.application.commands.reindexProjects;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;

public record ReindexProjectsCommand() implements ICommand<ApiResponse<Void>> {}
