package com.iyte_yazilim.proje_pazari.application.commands.createProject;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IDbContext;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IMapper;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IValidator;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.CreateProjectCommandResult;

public class CreateProjectHandler
        implements IRequestHandler<CreateProjectCommand, ApiResponse<CreateProjectCommandResult>> {

    private final IDbContext dbContext;
    private final IValidator<CreateProjectCommand> validator;
    private final IMapper mapper;

    public CreateProjectHandler(IDbContext dbContext, IValidator validator, IMapper mapper) {
        this.dbContext = dbContext;
        this.validator = validator;
        this.mapper = mapper;
    }

    public ApiResponse<CreateProjectCommandResult> handle(CreateProjectCommand command) {

        // TODO: Implement the logic to create a new project
        // return ApiResponse.success();
    }
}
