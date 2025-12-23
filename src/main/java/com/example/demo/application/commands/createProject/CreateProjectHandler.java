package com.example.demo.application.commands.createProject;

import com.example.demo.domain.interfaces.IDbContext;
import com.example.demo.domain.interfaces.IMapper;
import com.example.demo.domain.interfaces.IRequestHandler;
import com.example.demo.domain.interfaces.IValidator;
import com.example.demo.domain.models.ApiResponse;
import com.example.demo.domain.models.results.CreateProjectCommandResult;

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
