package com.iyte_yazilim.proje_pazari.application.commands.importUsersFromCsv;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.application.dtos.ImportResultDTO;
import org.springframework.web.multipart.MultipartFile;

public record ImportUsersFromCsvCommand(String csvContent, MultipartFile file)
        implements ICommand<ApiResponse<ImportResultDTO>> {
    public ImportUsersFromCsvCommand(String csvContent) {
        this(csvContent, null);
    }
}
