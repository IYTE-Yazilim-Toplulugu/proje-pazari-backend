package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.dtos.UserDto;
import com.iyte_yazilim.proje_pazari.application.queries.getAllUsers.GetAllUsersQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getUser.GetUserQuery;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final IRequestHandler<GetUserQuery, ApiResponse<UserDto>> getUserHandler;
    private final IRequestHandler<GetAllUsersQuery, ApiResponse<List<UserDto>>> getAllUsersHandler;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{userId}")
    @Operation(
            summary = "Get user by ID",
            description = "Retrieves user details by user ID"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    public ResponseEntity<ApiResponse<UserDto>> getUser(@PathVariable String userId) {

        ApiResponse<UserDto> response = getUserHandler.handle(new GetUserQuery(userId));

        HttpStatus status = switch (response.getCode()) {
            case SUCCESS -> HttpStatus.OK;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.OK;
        };

        return ResponseEntity.status(status).body(response);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(
            summary = "Get all users",
            description = "Retrieves a list of all users"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully"
            )
    })
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {

        ApiResponse<List<UserDto>> response = getAllUsersHandler.handle(new GetAllUsersQuery());

        return ResponseEntity.ok(response);
    }
}
