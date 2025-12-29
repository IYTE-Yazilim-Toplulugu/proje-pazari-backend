package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.commands.loginUser.LoginUserCommand;
import com.iyte_yazilim.proje_pazari.application.commands.registerUser.RegisterUserCommand;
import com.iyte_yazilim.proje_pazari.application.commands.resendVerificationEmail.ResendVerificationEmailCommand;
import com.iyte_yazilim.proje_pazari.application.commands.verifyEmail.VerifyEmailCommand;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.LoginUserResult;
import com.iyte_yazilim.proje_pazari.domain.models.results.RegisterUserResult;
import com.iyte_yazilim.proje_pazari.domain.models.results.VerifyEmailResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints for user registration and login")
public class AuthController {

    private final IRequestHandler<RegisterUserCommand, ApiResponse<RegisterUserResult>> registerUserHandler;
    private final IRequestHandler<LoginUserCommand, ApiResponse<LoginUserResult>> loginUserHandler;
    private final IRequestHandler<VerifyEmailCommand, ApiResponse<VerifyEmailResult>> verifyEmailHandler;
    private final IRequestHandler<ResendVerificationEmailCommand, ApiResponse<Void>> resendVerificationEmailHandler;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with the provided details"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or email already exists"
            )
    })
    public ResponseEntity<ApiResponse<RegisterUserResult>> register(
            @Valid @RequestBody RegisterUserCommand command) {

        ApiResponse<RegisterUserResult> response = registerUserHandler.handle(command);

        HttpStatus status = switch (response.getCode()) {
            case CREATED -> HttpStatus.CREATED;
            case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.OK;
        };

        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login user",
            description = "Authenticates a user and returns a token"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login successful"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid credentials"
            )
    })
    public ResponseEntity<ApiResponse<LoginUserResult>> login(
            @Valid @RequestBody LoginUserCommand command) {

        ApiResponse<LoginUserResult> response = loginUserHandler.handle(command);

        HttpStatus status = switch (response.getCode()) {
            case SUCCESS -> HttpStatus.OK;
            case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.OK;
        };

        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/verify-email")
    @Operation(
            summary = "Verify email address",
            description = "Verifies user's email using the verification token sent to their inbox"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Email verified successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired token"
            )
    })
    public ResponseEntity<ApiResponse<VerifyEmailResult>> verifyEmail(@RequestParam String token) {
        VerifyEmailCommand command = new VerifyEmailCommand(token);
        ApiResponse<VerifyEmailResult> response = verifyEmailHandler.handle(command);

        HttpStatus status = switch (response.getCode()) {
            case SUCCESS -> HttpStatus.OK;
            case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.OK;
        };

        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/resend-verification")
    @Operation(
            summary = "Resend verification email",
            description = "Sends a new verification email to the user"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Verification email sent"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "User not found or email already verified"
            )
    })
    public ResponseEntity<ApiResponse<Void>> resendVerification(@Valid @RequestBody ResendVerificationEmailCommand command) {
        ApiResponse<Void> response = resendVerificationEmailHandler.handle(command);

        HttpStatus status = switch (response.getCode()) {
            case SUCCESS -> HttpStatus.OK;
            case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.OK;
        };

        return ResponseEntity.status(status).body(response);
    }
}
