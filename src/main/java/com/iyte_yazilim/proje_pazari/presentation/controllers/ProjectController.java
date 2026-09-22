package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.commands.createProject.CreateProjectCommand;
import com.iyte_yazilim.proje_pazari.application.commands.deleteProject.DeleteProjectCommand;
import com.iyte_yazilim.proje_pazari.application.commands.updateProject.UpdateProjectCommand;
import com.iyte_yazilim.proje_pazari.application.commands.updateProjectStatus.UpdateProjectStatusCommand;
import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedProjectsResult;
import com.iyte_yazilim.proje_pazari.application.dtos.ProjectDetailDto;
import com.iyte_yazilim.proje_pazari.application.queries.getAllProjects.GetAllProjectsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getProject.GetProjectQuery;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.domain.models.results.CreateProjectCommandResult;
import com.iyte_yazilim.proje_pazari.domain.models.results.UpdateProjectStatusCommandResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
@Tag(
        name = "Projects",
        description =
                "Project management endpoints. "
                        + "Allows users to create, read, update, and delete projects.")
public class ProjectController extends BaseController {

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Create a new project",
            description =
                    "Creates a new project with the provided details. "
                            + "The authenticated user becomes the project owner. "
                            + "Requires authentication.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "201",
                        description = "Project created successfully",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = ApiResponse.class),
                                        examples =
                                                @ExampleObject(
                                                        name = "Success Response",
                                                        value =
                                                                """
                    {
                        "code": 2,
                        "message": "Project created successfully",
                        "data": {
                            "projectId": "01HQXV5KXBW9FYMN8CJZSP2R4H",
                            "projectName": "AI Chatbot Project",
                            "status": "DRAFT"
                        }
                    }
                    """))),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Invalid request data",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = ApiResponse.class),
                                        examples =
                                                @ExampleObject(
                                                        name = "Validation Error",
                                                        value =
                                                                """
                    {
                        "code": 9,
                        "errorCode": "VALIDATION_ERROR",
                        "message": "Validation error"
                    }
                    """))),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized - authentication required",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        examples =
                                                @ExampleObject(
                                                        name = "Unauthorized",
                                                        value =
                                                                """
                    {
                        "code": 5,
                        "errorCode": "UNAUTHORIZED",
                        "message": "Unauthorized access"
                    }
                    """))),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "500",
                        description = "Internal server error")
            })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Project creation details",
            required = true,
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreateProjectCommand.class),
                            examples =
                                    @ExampleObject(
                                            name = "Create Project Request",
                                            value =
                                                    """
            {
                "projectName": "AI Chatbot Project",
                "description": "Building an AI-powered chatbot for customer support using modern NLP techniques.",
                "summary": "AI chatbot with NLP capabilities",
                "maxTeamSize": 5,
                "requiredSkills": ["Python", "NLP", "Machine Learning", "FastAPI"],
                "category": "Artificial Intelligence",
                "deadline": "2025-06-15T23:59:59"
            }
            """)))
    public ResponseEntity<ApiResponse<CreateProjectCommandResult>> createProject(
            @RequestBody CreateProjectCommand command, Authentication auth) {
        return send(CreateProjectCommand.class, null, null, command, auth);
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    @Operation(
            summary = "Get all projects",
            description = "Retrieves a list of all projects. Public access.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Projects retrieved successfully",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = ApiResponse.class),
                                        examples =
                                                @ExampleObject(
                                                        name = "Success Response",
                                                        value =
                                                                """
                    {
                        "code": 0,
                        "message": "Projects retrieved successfully",
                        "data": {
                            "projects": [
                                {
                                    "projectId": "1",
                                    "projectName": "AI Chatbot Project",
                                    "description": "Building an AI-powered chatbot",
                                    "status": "ACTIVE"
                                }
                            ],
                            "currentPage": 0,
                            "totalPages": 1,
                            "totalElements": 1
                        }
                    }
                    """))),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "500",
                        description = "Internal server error")
            })
    public ResponseEntity<ApiResponse<PagedProjectsResult>> getAllProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) ProjectStatus status) {
        return send(new GetAllProjectsQuery(page, size, sortBy, sortDirection, status));
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("permitAll()")
    @Operation(
            summary = "Get project by ID",
            description = "Retrieves a specific project by its ID. Public access.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Project retrieved successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Project not found")
            })
    public ResponseEntity<ApiResponse<ProjectDetailDto>> getProject(
            @PathVariable String projectId) {
        return send(new GetProjectQuery(projectId));
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Update a project",
            description =
                    "Updates a project's details. Only the project owner can update their project.")
    public ResponseEntity<ApiResponse<ProjectDetailDto>> updateProject(
            @PathVariable String projectId,
            @Valid @RequestBody UpdateProjectCommand command,
            Authentication auth) {
        return send(
                UpdateProjectCommand.class, Map.of("projectId", projectId), null, command, auth);
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Delete a project",
            description = "Deletes a project. Only the project owner can delete their project.")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable String projectId, Authentication auth) {
        return send(DeleteProjectCommand.class, Map.of("projectId", projectId), null, null, auth);
    }

    @PatchMapping("/{projectId}/status")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Update project status",
            description = "Changes the status of a project. Only the project owner can do this.")
    public ResponseEntity<ApiResponse<UpdateProjectStatusCommandResult>> updateProjectStatus(
            @PathVariable String projectId,
            @RequestBody UpdateProjectStatusCommand command,
            Authentication auth) {
        return send(
                UpdateProjectStatusCommand.class,
                Map.of("projectId", projectId),
                null,
                command,
                auth);
    }
}
