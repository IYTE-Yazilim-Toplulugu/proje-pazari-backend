package com.iyte_yazilim.proje_pazari.application.commands.importProjectsFromCsv;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.ImportResultDTO;
import org.springframework.web.multipart.MultipartFile;

public record ImportProjectsFromCsvCommand(String csvContent, MultipartFile file)
        implements IRequest<ApiResponse<ImportResultDTO>> {
    public ImportProjectsFromCsvCommand(String csvContent) {
        this(csvContent, null);
    }
}
