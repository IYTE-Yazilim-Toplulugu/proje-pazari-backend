package com.iyte_yazilim.proje_pazari.application.dtos;

import java.util.List;

public record ImportResultDTO(
        int totalRows, int successCount, int failedCount, List<String> errors) {}
