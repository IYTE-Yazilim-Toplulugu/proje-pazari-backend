package com.iyte_yazilim.proje_pazari.domain.models.results;

import com.iyte_yazilim.proje_pazari.application.dtos.ApplicationDto;
import java.util.List;

public record PagedApplicationsResult(
        List<ApplicationDto> applications, int currentPage, int totalPages, long totalElements) {}
