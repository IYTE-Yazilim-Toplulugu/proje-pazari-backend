package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.commands.createProject.CreateProjectCommand;
import com.iyte_yazilim.proje_pazari.application.common.IMediator;
import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.CreateProjectCommandResult;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
public class ProjectController {

    private final IMediator mediator;
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    @PostMapping
    @PreAuthorize("isAuthenticated() and hasRole('PROJECT_OWNER')")
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
                        "code": "CREATED",
                        "message": "Project created successfully",
                        "data": {
                            "projectId": "01HQXV5KXBW9FYMN8CJZSP2R4H",
                            "title": "AI Chatbot Project",
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
                        "code": "BAD_REQUEST",
                        "message": "Title is required",
                        "data": null
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
                        "code": "UNAUTHORIZED",
                        "message": "Authentication required",
                        "data": null
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
                "title": "AI Chatbot Project",
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
                                            "code": "SUCCESS",
                                            "message": "Projects retrieved successfully",
                                            "data": [
                                                {
                                                    "id": "1",
                                                    "title": "AI Chatbot Project",
                                                    "description": "Building an AI-powered chatbot",
                                                    "status": "ACTIVE"
                                                }
                                            ]
                                        }
                                        """))),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "500",
                        description = "Internal server error")
            })
    public ResponseEntity<ApiResponse<List<Project>>> getAllProjects() {
        List<Project> projects =
                projectRepository.findAll().stream().map(projectMapper::entityToDomain).toList();

        return ResponseEntity.ok(ApiResponse.success(projects, "Projects retrieved successfully"));
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
    public ResponseEntity<ApiResponse<Project>> getProject(@PathVariable String projectId) {
        return projectRepository
                .findById(projectId)
                .map(
                        entity -> {
                            Project project = projectMapper.entityToDomain(entity);
                            return ResponseEntity.ok(
                                    ApiResponse.success(project, "Project retrieved successfully"));
                        })
                .orElse(ResponseEntity.notFound().build());
    }
}
