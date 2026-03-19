package com.iyte_yazilim.proje_pazari.application.queries.getAllProjects;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import java.util.List;

public record GetAllProjectsQuery() implements IRequest<ApiResponse<List<Project>>> {}
