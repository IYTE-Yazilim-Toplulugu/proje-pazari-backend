package com.iyte_yazilim.proje_pazari.application.queries.getProject;

public class GetProjectQueryHandler implements QueryHandler<GetProjectQuery, ProjectDetailDto> {

    @Override
    public ProjectDetailDto handle(GetProjectQuery query) {
        // Include owner info, applications count, status
    }
}
