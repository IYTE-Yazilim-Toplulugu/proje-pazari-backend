package com.iyte_yazilim.proje_pazari.application.queries.getAllProjects;

public record GetAllProjectsQuery(int page, int size, String sortBy, String sortDirection) {}
