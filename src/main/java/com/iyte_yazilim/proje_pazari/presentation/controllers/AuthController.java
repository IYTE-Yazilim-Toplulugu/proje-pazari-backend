package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.commands.forgotPassword.ForgotPasswordCommand;
import com.iyte_yazilim.proje_pazari.application.commands.loginUser.LoginUserCommand;
import com.iyte_yazilim.proje_pazari.application.commands.logout.LogoutCommand;
import com.iyte_yazilim.proje_pazari.application.commands.logout.LogoutRequest;
import com.iyte_yazilim.proje_pazari.application.commands.refreshToken.RefreshTokenCommand;
import com.iyte_yazilim.proje_pazari.application.commands.refreshToken.RefreshTokenResult;
import com.iyte_yazilim.proje_pazari.application.commands.registerUser.RegisterUserCommand;
import com.iyte_yazilim.proje_pazari.application.commands.resendVerificationEmail.ResendVerificationEmailCommand;
import com.iyte_yazilim.proje_pazari.application.commands.resetPassword.ResetPasswordCommand;
import com.iyte_yazilim.proje_pazari.application.commands.verifyEmail.VerifyEmailCommand;
import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.results.LoginUserResult;
import com.iyte_yazilim.proje_pazari.domain.models.results.RegisterUserResult;
import com.iyte_yazilim.proje_pazari.domain.models.results.VerifyEmailResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
@Tag(
        name = "Authentication",
        description =
                "Authentication endpoints for user registration and login. "
                        + "These endpoints are public and do not require authentication.")
public class AuthController extends BaseController {

    private final IRequestHandler<VerifyEmailCommand, ApiResponse<VerifyEmailResult>>
            verifyEmailHandler;
    private final IRequestHandler<ResendVerificationEmailCommand, ApiResponse<Void>>
            resendVerificationEmailHandler;

    public AuthController(
            IRequestHandler<VerifyEmailCommand, ApiResponse<VerifyEmailResult>> verifyEmailHandler,
            IRequestHandler<ResendVerificationEmailCommand, ApiResponse<Void>>
                    resendVerificationEmailHandler) {
        this.verifyEmailHandler = verifyEmailHandler;
        this.resendVerificationEmailHandler = resendVerificationEmailHandler;
    }

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
                        description =
                                "User registered successfully and email verification is required",
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
                                            "code": 11,
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
                                            "code": 9,
                                            "errorCode": "VALIDATION_ERROR",
                                            "message": "This email address is already registered"
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
        return send(command);
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
                                            "code": 0,
                                            "message": "Login successful",
                                            "data": {
                                                "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                "tokenType": "Bearer",
                                                "expiresIn": 86400000
                                                }
                                                }
                                        """))),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
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
                                            "code": 5,
                                            "errorCode": "UNAUTHORIZED",
                                            "message": "Invalid username or password"
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
        return send(command);
    }

    @GetMapping("/verify-email")
    @Operation(
            summary = "Verify email address",
            description = "Verifies user's email using the verification token sent to their inbox")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Email verified successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Invalid or expired token")
            })
    public ResponseEntity<ApiResponse<VerifyEmailResult>> verifyEmail(
            @Parameter(description = "Email verification token received via email", required = true)
                    @RequestParam
                    String token) {
        VerifyEmailCommand command = new VerifyEmailCommand(token);
        ApiResponse<VerifyEmailResult> response = verifyEmailHandler.handle(command);

        HttpStatus status =
                switch (response.getCode()) {
                    case SUCCESS -> HttpStatus.OK;
                    case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
                    default -> HttpStatus.OK;
                };

        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/resend-verification")
    @Operation(
            summary = "Resend verification email",
            description = "Sends a new verification email to the user")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Verification email sent"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "User not found or email already verified")
            })
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @Valid @RequestBody ResendVerificationEmailCommand command) {
        ApiResponse<Void> response = resendVerificationEmailHandler.handle(command);

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
                    "Issues a new access token using a valid refresh token. "
                            + "Use this when the current access token has expired.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Token refreshed successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Invalid or expired refresh token")
            })
    public ResponseEntity<ApiResponse<RefreshTokenResult>> refreshToken(
            @Parameter(
                            description = "Refresh token obtained during login",
                            required = true,
                            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                    @RequestParam
                    String refreshToken) {
        return send(new RefreshTokenCommand(refreshToken));
    }

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Request password reset",
            description =
                    "Sends a password reset link to the provided email address. "
                            + "Always returns success to prevent user enumeration.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Reset email sent if account exists",
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
                                            "message": "A password reset link has been sent to your email address",
                                            "data": null
                                        }
                                        """))),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Invalid request — email field fails @Email validation",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = ApiResponse.class),
                                        examples =
                                                @ExampleObject(
                                                        name = "Invalid Email",
                                                        value =
                                                                """
                                        {
                                            "code": 9,
                                            "errorCode": "VALIDATION_ERROR",
                                            "message": "Invalid email address"
                                        }
                                        """)))
            })
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordCommand command) {
        return send(command);
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Reset password using token",
            description = "Resets the user's password using the token received by email.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Password reset successfully",
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
                                            "message": "Your password has been reset successfully",
                                            "data": null
                                        }
                                        """))),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Invalid or expired token, or weak password",
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
                                            "code": 4,
                                            "errorCode": "INVALID_VERIFICATION_TOKEN",
                                            "message": "Invalid or already used password reset link"
                                        }
                                        """)))
            })
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordCommand command) {
        return send(command);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Logout user",
            description =
                    "Revokes the refresh token and blacklists the access token. "
                            + "The refresh token is supplied in the request body. "
                            + "The access token is read from the Authorization header and blacklisted in Redis until it expires naturally.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Logout successful"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Refresh token missing, invalid, or does not belong to user")
            })
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest body,
            Authentication auth,
            HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String accessToken =
                (authHeader != null && authHeader.startsWith("Bearer "))
                        ? authHeader.substring(7)
                        : null;
        String userId = getCurrentUserId(auth);
        return send(new LogoutCommand(accessToken, body.refreshToken(), userId));
    }
}
