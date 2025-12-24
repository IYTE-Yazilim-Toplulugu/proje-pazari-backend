package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.commands.createProject.CreateProjectCommand;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.CreateProjectCommandResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project management endpoints")
public class ProjectController {

    private final IRequestHandler<CreateProjectCommand, ApiResponse<CreateProjectCommandResult>> createProjectHandler;

    @PostMapping
    @Operation(
            summary = "Create a new project",
            description = "Creates a new project with the provided details"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Project created successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public ResponseEntity<ApiResponse<CreateProjectCommandResult>> createProject(
            @Valid @RequestBody CreateProjectCommand command) {

        ApiResponse<CreateProjectCommandResult> response = createProjectHandler.handle(command);

        HttpStatus status = switch (response.getCode()) {
            case CREATED -> HttpStatus.CREATED;
            case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.OK;
        };

        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Check if the project service is running"
    )
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Project service is running");
    }
}
