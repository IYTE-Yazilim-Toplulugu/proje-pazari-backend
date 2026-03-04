package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.commands.reviewApplication.ReviewApplicationCommand;
import com.iyte_yazilim.proje_pazari.application.commands.submitApplication.SubmitApplicationCommand;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.ReviewApplicationCommandResult;
import com.iyte_yazilim.proje_pazari.domain.models.results.SubmitApplicationCommandResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Applications", description = "Project application management endpoints.")
public class ApplicationController extends BaseController {

    @PostMapping("/projects/{projectId}/applications")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Submit an application to a project",
            description =
                    "Submits an application to join the specified project. Requires authentication.")
    public ResponseEntity<ApiResponse<SubmitApplicationCommandResult>> submitApplication(
            @PathVariable String projectId, Authentication auth) {
        return send(
                SubmitApplicationCommand.class, Map.of("projectId", projectId), null, null, auth);
    }

    @PutMapping("/applications/{applicationId}/review")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Review a project application",
            description = "Approves or rejects a project application. Requires authentication.")
    public ResponseEntity<ApiResponse<ReviewApplicationCommandResult>> reviewApplication(
            @PathVariable String applicationId,
            @RequestBody ReviewApplicationCommand command,
            Authentication auth) {
        return send(
                ReviewApplicationCommand.class,
                Map.of("applicationId", applicationId),
                null,
                command,
                auth);
    }
}
