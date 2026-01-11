package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.commands.createProject.CreateProjectCommand;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.CreateProjectCommandResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(
        name = "Projects",
        description =
                "Project management endpoints. "
                        + "Allows users to create, read, update, and delete projects.")
public class ProjectController {

    private final IRequestHandler<CreateProjectCommand, ApiResponse<CreateProjectCommandResult>>
            createProjectHandler;

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
            @Valid @RequestBody CreateProjectCommand command) {

        ApiResponse<CreateProjectCommandResult> response = createProjectHandler.handle(command);

        HttpStatus status =
                switch (response.getCode()) {
                    case CREATED -> HttpStatus.CREATED;
                    case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
                    case NOT_FOUND -> HttpStatus.NOT_FOUND;
                    case INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
                    default -> HttpStatus.OK;
                };

        return ResponseEntity.status(status).body(response);
    }
}
