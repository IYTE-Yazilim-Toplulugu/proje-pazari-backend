package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.commands.changePassword.ChangePasswordCommand;
import com.iyte_yazilim.proje_pazari.application.commands.deactivateAccount.DeactivateAccountCommand;
import com.iyte_yazilim.proje_pazari.application.commands.updateUserProfile.UpdateUserProfileCommand;
import com.iyte_yazilim.proje_pazari.application.commands.uploadProfilePicture.UploadProfilePictureCommand;
import com.iyte_yazilim.proje_pazari.application.dtos.UserDto;
import com.iyte_yazilim.proje_pazari.application.dtos.UserProfileDTO;
import com.iyte_yazilim.proje_pazari.application.queries.getAllUsers.GetAllUsersQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getMyApplications.GetMyApplicationsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getUserProfile.GetUserProfileQuery;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.MyApplicationResult;
import com.iyte_yazilim.proje_pazari.presentation.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@Tag(
        name = "User",
        description =
                "User management endpoints. Includes profile management, password change, "
                        + "profile picture upload, and account deactivation. "
                        + "Most endpoints require authentication.")
@PreAuthorize("isAuthenticated()")
public class UserController extends BaseController {

    @GetMapping
    @PreAuthorize("isAuthenticated() and hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get all users", description = "Retrieves a list of all users")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Users retrieved successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized")
            })
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        return send(new GetAllUsersQuery());
    }

    @GetMapping("/{userId}")
    @PreAuthorize("permitAll()")
    @Operation(
            summary = "Get user profile by ID",
            description = "Retrieves any user's public profile with statistics. Public access.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Profile retrieved successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "User not found")
            })
    public ResponseEntity<ApiResponse<UserProfileDTO>> getUserProfile(@PathVariable String userId) {
        return send(new GetUserProfileQuery(userId));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Get current user profile",
            description = "Retrieves the authenticated user's profile")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Profile retrieved successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized")
            })
    public ResponseEntity<ApiResponse<UserProfileDTO>> getCurrentUserProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        return send(new GetUserProfileQuery(principal.getUserId()));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me")
    @Operation(
            summary = "Update user profile",
            description = "Updates the authenticated user's profile information")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Profile updated successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Validation error"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized")
            })
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(
            @RequestBody @Valid UpdateUserProfileCommand command, Authentication auth) {
        return send(UpdateUserProfileCommand.class, null, null, command, auth);
    }

    @PostMapping(value = "/me/profile-picture", consumes = "multipart/form-data")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Upload profile picture",
            description = "Uploads a new profile picture for the authenticated user",
            requestBody =
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            content =
                                    @io.swagger.v3.oas.annotations.media.Content(
                                            mediaType = "multipart/form-data",
                                            schema =
                                                    @io.swagger.v3.oas.annotations.media.Schema(
                                                            type = "object",
                                                            implementation = Object.class),
                                            encoding =
                                                    @io.swagger.v3.oas.annotations.media.Encoding(
                                                            name = "file",
                                                            contentType =
                                                                    "image/jpeg, image/png, image/gif, image/webp"))))
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Profile picture uploaded successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Invalid file or validation error"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized")
            })
    public ResponseEntity<ApiResponse<String>> uploadProfilePicture(
            @io.swagger.v3.oas.annotations.Parameter(
                            description = "Profile picture file to upload",
                            required = true,
                            content =
                                    @io.swagger.v3.oas.annotations.media.Content(
                                            mediaType = "multipart/form-data"))
                    @RequestParam("file")
                    MultipartFile file,
            Authentication auth) {
        return send(
                UploadProfilePictureCommand.class, null, null, null, auth, Map.of("file", file));
    }

    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Change password",
            description = "Changes the authenticated user's password")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Password changed successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Validation error or incorrect current password"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized")
            })
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody @Valid ChangePasswordCommand command, Authentication auth) {
        return send(ChangePasswordCommand.class, null, null, command, auth);
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Deactivate account",
            description = "Deactivates the authenticated user's account")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Account deactivated successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized")
            })
    public ResponseEntity<ApiResponse<Void>> deactivateAccount(
            @RequestParam(required = false) String reason, Authentication auth) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("reason", reason);
        return send(DeactivateAccountCommand.class, null, queryParams, null, auth);
    }

    @GetMapping("/me/applications")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Get my applications",
            description = "Returns all applications submitted by the authenticated user.")
    public ResponseEntity<ApiResponse<List<MyApplicationResult>>> getMyApplications(
            Authentication auth) {
        return send(GetMyApplicationsQuery.class, null, null, null, auth);
    }
}
