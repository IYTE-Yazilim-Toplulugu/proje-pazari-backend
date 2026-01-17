package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.commands.loginUser.LoginUserCommand;
import com.iyte_yazilim.proje_pazari.application.commands.registerUser.RegisterUserCommand;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.LoginUserResult;
import com.iyte_yazilim.proje_pazari.domain.models.results.RegisterUserResult;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.security.service.RefreshTokenService;
import com.iyte_yazilim.proje_pazari.presentation.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 *
 * <p>Provides endpoints for:
 *
 * <ul>
 *   <li>User registration
 *   <li>User login
 * </ul>
 *
 * <h2>Base Path:</h2>
 *
 * {@code /api/v1/auth}
 *
 * <h2>Authentication:</h2>
 *
 * <p>These endpoints are public and do not require authentication.
 *
 * <h2>Example Requests:</h2>
 *
 * <pre>{@code
 * // Registration
 * POST /api/v1/auth/register
 * Content-Type: application/json
 * {"email":"student@iyte.edu.tr","password":"Pass123!","firstName":"John","lastName":"Doe"}
 *
 * // Login
 * POST /api/v1/auth/login
 * Content-Type: application/json
 * {"email":"student@iyte.edu.tr","password":"Pass123!"}
 * }</pre>
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2024-01-01
 * @see RegisterUserCommand
 * @see LoginUserCommand
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentication",
        description =
                "Authentication endpoints for user registration and login. "
                        + "These endpoints are public and do not require authentication.")
public class AuthController {

    private final IRequestHandler<RegisterUserCommand, ApiResponse<RegisterUserResult>>
            registerUserHandler;
    private final IRequestHandler<LoginUserCommand, ApiResponse<LoginUserResult>> loginUserHandler;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final MessageService messageService;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description =
                    "Creates a new user account with the provided details. "
                            + "Email must be unique and password must meet security requirements. "
                            + "Only IYTE email domains (@std.iyte.edu.tr, @iyte.edu.tr) are accepted.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "201",
                        description = "User registered successfully",
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
                                            "message": "User registered successfully",
                                            "data": {
                                                "userId": "01HQXV5KXBW9FYMN8CJZSP2R4G",
                                                "email": "student@std.iyte.edu.tr",
                                                "firstName": "John",
                                                "lastName": "Doe"
                                            }
                                        }
                                        """))),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Invalid request data or email already exists",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = ApiResponse.class),
                                        examples =
                                                @ExampleObject(
                                                        name = "Email Exists",
                                                        value =
                                                                """
                                        {
                                            "code": "BAD_REQUEST",
                                            "message": "Email already exists",
                                            "data": null
                                        }
                                        """)))
            })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "User registration details",
            required = true,
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RegisterUserCommand.class),
                            examples =
                                    @ExampleObject(
                                            name = "Registration Request",
                                            value =
                                                    """
                        {
                            "email": "student@std.iyte.edu.tr",
                            "password": "SecurePass123!",
                            "firstName": "John",
                            "lastName": "Doe"
                        }
                        """)))
    public ResponseEntity<ApiResponse<RegisterUserResult>> register(
            @Valid @RequestBody RegisterUserCommand command) {

        ApiResponse<RegisterUserResult> response = registerUserHandler.handle(command);

        HttpStatus status =
                switch (response.getCode()) {
                    case CREATED -> HttpStatus.CREATED;
                    case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
                    default -> HttpStatus.OK;
                };

        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login user",
            description =
                    "Authenticates a user with email and password. "
                            + "Returns a JWT token on successful authentication. "
                            + "The token should be included in the Authorization header for protected endpoints.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Login successful",
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
                                            "message": "Login successful",
                                            "data": {
                                                "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                "tokenType": "Bearer",
                                                "expiresIn": 86400000
                                            }
                                        }
                                        """))),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Invalid credentials",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = ApiResponse.class),
                                        examples =
                                                @ExampleObject(
                                                        name = "Invalid Credentials",
                                                        value =
                                                                """
                                        {
                                            "code": "BAD_REQUEST",
                                            "message": "Invalid email or password",
                                            "data": null
                                        }
                                        """)))
            })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "User login credentials",
            required = true,
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginUserCommand.class),
                            examples =
                                    @ExampleObject(
                                            name = "Login Request",
                                            value =
                                                    """
                        {
                            "email": "student@std.iyte.edu.tr",
                            "password": "SecurePass123!"
                        }
                        """)))
    public ResponseEntity<ApiResponse<LoginUserResult>> login(
            @Valid @RequestBody LoginUserCommand command) {

        ApiResponse<LoginUserResult> response = loginUserHandler.handle(command);

        HttpStatus status =
                switch (response.getCode()) {
                    case SUCCESS -> HttpStatus.OK;
                    case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
                    default -> HttpStatus.OK;
                };

        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description =
                    "Uses a valid refresh token to obtain a new access token and refresh token. "
                            + "Implements token rotation - the old refresh token is revoked and a new one is issued.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Token refreshed successfully",
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
                                            "message": "Token refreshed successfully",
                                            "data": {
                                                "userId": "01HQXV5KXBW9FYMN8CJZSP2R4G",
                                                "email": "student@std.iyte.edu.tr",
                                                "firstName": "John",
                                                "lastName": "Doe",
                                                "role": "USER",
                                                "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                "refreshToken": "new-refresh-token-uuid...",
                                                "expiresIn": 86400000
                                            }
                                        }
                                        """))),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Invalid or expired refresh token",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = ApiResponse.class),
                                        examples =
                                                @ExampleObject(
                                                        name = "Invalid Token",
                                                        value =
                                                                """
                                        {
                                            "code": "BAD_REQUEST",
                                            "message": "Invalid or expired refresh token",
                                            "data": null
                                        }
                                        """)))
            })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Refresh token request",
            required = true,
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples =
                                    @ExampleObject(
                                            name = "Refresh Request",
                                            value =
                                                    """
                        {
                            "refreshToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890-a1b2c3d4-e5f6-7890-abcd-ef1234567890"
                        }
                        """)))
    public ResponseEntity<ApiResponse<LoginUserResult>> refresh(
            @RequestBody @Valid RefreshTokenRequest request) {

        // Validate refresh token
        Optional<String> userIdOpt = refreshTokenService.validateRefreshToken(request.refreshToken());

        if (userIdOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest("Invalid or expired refresh token"));
        }

        String userId = userIdOpt.get();

        // Find user
        UserEntity user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new IllegalStateException("User not found"));

        // Check if account is still active
        if (user.getIsActive() == null || !user.getIsActive()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.badRequest("Account is deactivated"));
        }

        // Generate new tokens
        String role = user.getRole() != null ? user.getRole().toString() : "USER";
        String newAccessToken = jwtUtil.generateToken(user.getId(), user.getEmail(), role);
        String newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

        // Revoke old refresh token
        refreshTokenService.revokeRefreshToken(request.refreshToken());

        // Create result
        var result =
                new LoginUserResult(
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        role,
                        newAccessToken,
                        newRefreshToken,
                        jwtExpiration);

        return ResponseEntity.ok(ApiResponse.success(result, "Token refreshed successfully"));
    }

    // Inner DTO for refresh request
    public record RefreshTokenRequest(@Valid @jakarta.validation.constraints.NotBlank String refreshToken) {}
}
