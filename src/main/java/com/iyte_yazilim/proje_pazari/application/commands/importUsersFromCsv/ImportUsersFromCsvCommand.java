package com.iyte_yazilim.proje_pazari.application.commands.importUsersFromCsv;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.ImportResultDTO;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;

public record ImportUsersFromCsvCommand(String csvContent)
        implements IRequest<ApiResponse<ImportResultDTO>> {}
