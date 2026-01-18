package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.commands.changePassword.ChangePasswordCommand;
import com.iyte_yazilim.proje_pazari.application.commands.deactivateAccount.DeactivateAccountCommand;
import com.iyte_yazilim.proje_pazari.application.commands.updateUserProfile.UpdateUserProfileCommand;
import com.iyte_yazilim.proje_pazari.application.commands.uploadProfilePicture.UploadProfilePictureCommand;
import com.iyte_yazilim.proje_pazari.application.dtos.UserDto;
import com.iyte_yazilim.proje_pazari.application.dtos.UserProfileDTO;
import com.iyte_yazilim.proje_pazari.application.queries.getAllUsers.GetAllUsersQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getCurrentUserProfile.GetCurrentUserProfileQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getUserProfile.GetUserProfileQuery;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(
        name = "User",
        description =
                "User management endpoints. Includes profile management, password change, "
                        + "profile picture upload, and account deactivation. "
                        + "Most endpoints require authentication.")
public class UserController extends BaseController {

    private final IRequestHandler<GetCurrentUserProfileQuery, ApiResponse<UserProfileDTO>>
            getCurrentUserProfileHandler;
    private final IRequestHandler<GetUserProfileQuery, ApiResponse<UserProfileDTO>>
            getUserProfileHandler;
    private final IRequestHandler<UpdateUserProfileCommand, ApiResponse<UserDto>>
            updateUserProfileHandler;
    private final IRequestHandler<UploadProfilePictureCommand, ApiResponse<String>>
            uploadProfilePictureHandler;
    private final IRequestHandler<ChangePasswordCommand, ApiResponse<Void>> changePasswordHandler;
    private final IRequestHandler<DeactivateAccountCommand, ApiResponse<Void>>
            deactivateAccountHandler;
    private final IRequestHandler<GetAllUsersQuery, ApiResponse<List<UserDto>>> getAllUsersHandler;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{userId}")
    @Operation(
            summary = "Get user profile by ID",
            description = "Retrieves any user's public profile with statistics")
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
        ApiResponse<UserProfileDTO> response =
                getUserProfileHandler.handle(new GetUserProfileQuery(userId));

        HttpStatus status =
                switch (response.getCode()) {
                    case SUCCESS -> HttpStatus.OK;
                    case NOT_FOUND -> HttpStatus.NOT_FOUND;
                    default -> HttpStatus.OK;
                };

        return ResponseEntity.status(status).body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
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
        String userId = getCurrentUserId(auth);

        UpdateUserProfileCommand updatedCommand =
                new UpdateUserProfileCommand(
                        userId,
                        command.firstName(),
                        command.lastName(),
                        command.description(),
                        command.linkedinUrl(),
                        command.githubUrl(),
                        command.preferredLanguage());

        ApiResponse<UserDto> response = updateUserProfileHandler.handle(updatedCommand);

        HttpStatus status =
                switch (response.getCode()) {
                    case SUCCESS -> HttpStatus.OK;
                    case VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
                    case NOT_FOUND -> HttpStatus.NOT_FOUND;
                    default -> HttpStatus.OK;
                };

        return ResponseEntity.status(status).body(response);
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
        String userId = getCurrentUserId(auth);

        UploadProfilePictureCommand command = new UploadProfilePictureCommand(userId, file);

        ApiResponse<String> response = uploadProfilePictureHandler.handle(command);

        HttpStatus status =
                switch (response.getCode()) {
                    case SUCCESS -> HttpStatus.OK;
                    case VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
                    case NOT_FOUND -> HttpStatus.NOT_FOUND;
                    default -> HttpStatus.INTERNAL_SERVER_ERROR;
                };

        return ResponseEntity.status(status).body(response);
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
        String userId = getCurrentUserId(auth);

        ChangePasswordCommand updatedCommand =
                new ChangePasswordCommand(
                        userId,
                        command.currentPassword(),
                        command.newPassword(),
                        command.confirmPassword());

        ApiResponse<Void> response = changePasswordHandler.handle(updatedCommand);

        HttpStatus status =
                switch (response.getCode()) {
                    case SUCCESS -> HttpStatus.OK;
                    case VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
                    case NOT_FOUND -> HttpStatus.NOT_FOUND;
                    default -> HttpStatus.OK;
                };

        return ResponseEntity.status(status).body(response);
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
        String userId = getCurrentUserId(auth);

        DeactivateAccountCommand command = new DeactivateAccountCommand(userId, reason);

        ApiResponse<Void> response = deactivateAccountHandler.handle(command);

        HttpStatus status =
                switch (response.getCode()) {
                    case SUCCESS -> HttpStatus.OK;
                    case NOT_FOUND -> HttpStatus.NOT_FOUND;
                    default -> HttpStatus.OK;
                };

        return ResponseEntity.status(status).body(response);
    }
}
