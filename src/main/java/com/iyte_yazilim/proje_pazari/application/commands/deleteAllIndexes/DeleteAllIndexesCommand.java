package com.iyte_yazilim.proje_pazari.application.commands.deleteAllIndexes;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ICommand;

public record DeleteAllIndexesCommand() implements ICommand<ApiResponse<Void>> {}
