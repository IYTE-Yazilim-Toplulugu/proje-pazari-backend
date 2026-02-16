package com.iyte_yazilim.proje_pazari.application.commands.importProjectsFromCsv;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.ImportResultDTO;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;

public record ImportProjectsFromCsvCommand(String csvContent)
        implements IRequest<ApiResponse<ImportResultDTO>> {}
