package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.commands.createProject.CreateProjectCommand;
import com.iyte_yazilim.proje_pazari.application.commands.deleteProject.DeleteProjectCommand;
import com.iyte_yazilim.proje_pazari.application.commands.updateProject.UpdateProjectCommand;
import com.iyte_yazilim.proje_pazari.application.commands.updateProjectStatus.UpdateProjectStatusCommand;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedProjectsResult;
import com.iyte_yazilim.proje_pazari.application.dtos.ProjectDetailDto;
import com.iyte_yazilim.proje_pazari.application.queries.getAllProjects.GetAllProjectsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getProject.GetProjectQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getUserProjects.GetUserProjectsQuery;
import com.iyte_yazilim.proje_pazari.domain.models.results.CreateProjectCommandResult;
import com.iyte_yazilim.proje_pazari.domain.models.results.UpdateProjectStatusCommandResult;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
                    "Creates a new project. The ownerId is resolved server-side from the"
                            + " authenticated user's bearer token — do not include it in the"
                            + " request body.")
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
                            "projectName": "AI Chatbot Project",
                            "description": "Building an AI-powered chatbot for customer support using modern NLP techniques.",
                            "ownerId": "01HQXV5KXBW9FYMN8CJZSP2R4G"
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
                        "message": "Project name is required",
                        "data": null
                    }
                    """))),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized")
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
                "maxTeamSize": 5,
                "requiredSkills": ["Python", "NLP", "Machine Learning", "FastAPI"],
                "category": "Artificial Intelligence",
                "deadline": "2026-06-15T23:59:59"
            }
            """)))
    public ResponseEntity<ApiResponse<CreateProjectCommandResult>> createProject(
            @Valid @RequestBody CreateProjectCommand command, Authentication auth) {
        return send(CreateProjectCommand.class, null, null, command, auth);
    }

    @GetMapping
    @Operation(summary = "Get all projects with pagination")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Projects retrieved successfully")
            })
    public ResponseEntity<ApiResponse<PagedProjectsResult>> getAllProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        return send(new GetAllProjectsQuery(page, size, sortBy, sortDirection));
    }

    @GetMapping("/{projectId}")
    @Operation(summary = "Get a project by ID")
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
            @Parameter(
                            description = "Unique project ID",
                            required = true,
                            example = "01HQXV5KXBW9FYMN8CJZSP2R4H")
                    @PathVariable
                    String projectId) {
        return send(new GetProjectQuery(projectId));
    }

    @GetMapping("/mine")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get projects owned by the authenticated user")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Projects retrieved successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized")
            })
    public ResponseEntity<ApiResponse<PagedProjectsResult>> getMyProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            Authentication auth) {
        String userId = getCurrentUserId(auth);
        return send(new GetUserProjectsQuery(userId, page, size, sortBy, sortDirection));
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("isAuthenticated() and hasRole('PROJECT_OWNER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update a project")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Project updated successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Invalid request data"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Not the project owner"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Project not found")
            })
    public ResponseEntity<ApiResponse<ProjectDetailDto>> updateProject(
            @PathVariable String projectId,
            @Valid @RequestBody UpdateProjectCommand command,
            Authentication auth) {
        return send(
                UpdateProjectCommand.class, Map.of("projectId", projectId), null, command, auth);
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("isAuthenticated() and hasRole('PROJECT_OWNER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete a project")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Project deleted successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Not the project owner"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Project not found")
            })
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable String projectId, Authentication auth) {
        return send(DeleteProjectCommand.class, Map.of("projectId", projectId), null, null, auth);
    }

    @PatchMapping("/{projectId}/status")
    @PreAuthorize("isAuthenticated() and hasRole('PROJECT_OWNER')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update a project's status")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Status updated successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Invalid status transition"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Project not found")
            })
    public ResponseEntity<ApiResponse<UpdateProjectStatusCommandResult>> updateProjectStatus(
            @PathVariable String projectId,
            @Valid @RequestBody UpdateProjectStatusCommand command,
            Authentication auth) {
        return send(
                UpdateProjectStatusCommand.class,
                Map.of("projectId", projectId),
                null,
                command,
                auth);
    }
}
