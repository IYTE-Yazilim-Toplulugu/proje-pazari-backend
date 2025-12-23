package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.commands.loginUser.LoginUserCommand;
import com.iyte_yazilim.proje_pazari.application.commands.registerUser.RegisterUserCommand;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.LoginUserResult;
import com.iyte_yazilim.proje_pazari.domain.models.results.RegisterUserResult;
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
}
