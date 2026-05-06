package com.iyte_yazilim.proje_pazari.application.commands.importProjectsFromCsv;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.application.dtos.ImportResultDTO;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import org.springframework.web.multipart.MultipartFile;

public record ImportProjectsFromCsvCommand(String csvContent, MultipartFile file)
        implements ICommand<ApiResponse<ImportResultDTO>> {
    public ImportProjectsFromCsvCommand(String csvContent) {
        this(csvContent, null);
    }
}
