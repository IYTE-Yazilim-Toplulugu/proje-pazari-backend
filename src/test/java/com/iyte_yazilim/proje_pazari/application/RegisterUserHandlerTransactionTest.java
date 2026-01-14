package com.iyte_yazilim.proje_pazari.application;

import static org.junit.jupiter.api.Assertions.*;

import com.iyte_yazilim.proje_pazari.application.commands.registerUser.RegisterUserCommand;
import com.iyte_yazilim.proje_pazari.application.commands.registerUser.RegisterUserHandler;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.RegisterUserResult;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Rollback
class RegisterUserHandlerTransactionTest {

    @Autowired private RegisterUserHandler handler;

    @Autowired private UserRepository userRepository;

    @Test
    void shouldRollbackTransaction_whenExceptionThrown() {
        // Given - try to register with duplicate email
        String email = "duplicate-test-" + UUID.randomUUID() + "@std.iyte.edu.tr";
        RegisterUserCommand command = new RegisterUserCommand(email, "password123", "Test", "User");

        // First registration should succeed
        ApiResponse<RegisterUserResult> firstResponse = handler.handle(command);
        assertEquals(ResponseCode.CREATED, firstResponse.getCode());

        // When - try to register with same email (should fail due to unique constraint)
        RegisterUserCommand duplicateCommand =
                new RegisterUserCommand(email, "password456", "Another", "User");

        // Then - should return error response for duplicate email
        ApiResponse<RegisterUserResult> response = handler.handle(duplicateCommand);
        assertNotNull(response);
        assertEquals(
                ResponseCode.BAD_REQUEST,
                response.getCode(),
                "Should return bad request for duplicate email");
    }

    @Test
    void shouldCommitTransaction_whenSuccessful() {
        // Given
        String email = "test-success-" + UUID.randomUUID() + "@std.iyte.edu.tr";
        RegisterUserCommand command = new RegisterUserCommand(email, "password123", "Test", "User");

        // When
        ApiResponse<RegisterUserResult> response = handler.handle(command);

        // Then
        assertNotNull(response);
        assertEquals(ResponseCode.CREATED, response.getCode());
        assertTrue(userRepository.existsByEmail(email), "User was not persisted to the database!");
    }
}
