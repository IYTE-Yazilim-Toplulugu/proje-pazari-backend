package com.iyte_yazilim.proje_pazari.application.commands.deleteAllIndexes;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;

public record DeleteAllIndexesCommand() implements ICommand<ApiResponse<Void>> {}
