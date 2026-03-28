package com.iyte_yazilim.proje_pazari.application.dtos;

import java.util.List;

public record PagedProjectsResult(
        List<ProjectDetailDto> projects, int currentPage, int totalPages, long totalElements) {}
