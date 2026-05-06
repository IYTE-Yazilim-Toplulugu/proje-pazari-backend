package com.iyte_yazilim.proje_pazari.application.commands.reindexProjects;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ICommand;

public record ReindexProjectsCommand() implements ICommand<ApiResponse<Void>> {}
