package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.commands.changePassword.ChangePasswordCommand;
import com.iyte_yazilim.proje_pazari.application.commands.deactivateAccount.DeactivateAccountCommand;
import com.iyte_yazilim.proje_pazari.application.commands.updateLanguage.UpdateLanguageCommand;
import com.iyte_yazilim.proje_pazari.application.commands.updateUserProfile.UpdateUserProfileCommand;
import com.iyte_yazilim.proje_pazari.application.commands.uploadProfilePicture.UploadProfilePictureCommand;
import com.iyte_yazilim.proje_pazari.application.dtos.UserDto;
import com.iyte_yazilim.proje_pazari.application.dtos.UserProfileDTO;
import com.iyte_yazilim.proje_pazari.application.queries.getAllUsers.GetAllUsersQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getCurrentUserProfile.GetCurrentUserProfileQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getUserProfile.GetUserProfileQuery;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User management endpoints")
@Validated
public class UserController {

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
    private final IRequestHandler<UpdateLanguageCommand, ApiResponse<Void>>
            updateLanguageHandler;
    private final UserRepository userRepository;
    private final IRequestHandler<GetAllUsersQuery, ApiResponse<List<UserDto>>> getAllUsersHandler;

    /**
     * Resolves email from JWT token to user ID.
     * The JWT token contains email as the subject, not the user ID.
     */
    private String resolveUserIdFromEmail(String email) {
        return userRepository.findByEmail(email).map(user -> user.getId()).orElse(null);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
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
        ApiResponse<List<UserDto>> response = getAllUsersHandler.handle(new GetAllUsersQuery());
        HttpStatus status =
                switch (response.getCode()) {
                    case SUCCESS -> HttpStatus.OK;
                    case NOT_FOUND -> HttpStatus.NOT_FOUND;
                    default -> HttpStatus.OK;
                };
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Get current user profile",
            description = "Retrieves the authenticated user's complete profile with statistics")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Profile retrieved successfully"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized")
            })
    public ResponseEntity<ApiResponse<UserProfileDTO>> getCurrentUserProfile(Authentication auth) {
        String email = auth.getName();
        String userId = resolveUserIdFromEmail(email);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notFound("User not found"));
        }
        ApiResponse<UserProfileDTO> response =
                getCurrentUserProfileHandler.handle(new GetCurrentUserProfileQuery(userId));
        HttpStatus status =
                switch (response.getCode()) {
                    case SUCCESS -> HttpStatus.OK;
                    case NOT_FOUND -> HttpStatus.NOT_FOUND;
                    default -> HttpStatus.OK;
                };
        return ResponseEntity.status(status).body(response);
    }

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

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
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
        String email = auth.getName();
        String userId = resolveUserIdFromEmail(email);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notFound("User not found"));
        }
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

    @PutMapping("/me/language")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Update user's preferred language",
            description =
                    "Updates the authenticated user's preferred language for API responses. "
                            + "This setting determines the language of all localized messages. "
                            + "Supported languages: Turkish (tr), English (en).")
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description =
                                    "Language preference updated successfully. "
                                            + "The success message will be returned in the NEW language."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description =
                                    "Invalid language value. Only 'tr' (Turkish) and 'en' (English) are supported."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "User not found")
            })
    public ResponseEntity<ApiResponse<Void>> updateLanguage(
            @Parameter(
                    description = "Language code (tr for Turkish, en for English)",
                    required = true,
                    example = "en")
            @RequestParam
            @Pattern(
                    regexp = "^(tr|en)$",
                    message = "Language must be either 'tr' or 'en'")
            String language,
            Authentication auth) {

        // Resolve user ID from authenticated email
        String email = auth.getName();
        String userId = resolveUserIdFromEmail(email);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notFound("User not found"));
        }

        // Create command
        UpdateLanguageCommand command = new UpdateLanguageCommand(userId, language);

        // Handle the command
        ApiResponse<Void> response = updateLanguageHandler.handle(command);

        // Map response code to HTTP status
        HttpStatus status =
                switch (response.getCode()) {
                    case SUCCESS -> HttpStatus.OK;
                    case VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
                    case NOT_FOUND -> HttpStatus.NOT_FOUND;
                    default -> HttpStatus.OK;
                };

        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/me/profile-picture")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Upload profile picture",
            description = "Uploads a new profile picture for the authenticated user")
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
            @RequestParam("file") MultipartFile file, Authentication auth) {
        String email = auth.getName();
        String userId = resolveUserIdFromEmail(email);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notFound("User not found"));
        }
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
        String email = auth.getName();
        String userId = resolveUserIdFromEmail(email);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notFound("User not found"));
        }
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
        String email = auth.getName();
        String userId = resolveUserIdFromEmail(email);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notFound("User not found"));
        }
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