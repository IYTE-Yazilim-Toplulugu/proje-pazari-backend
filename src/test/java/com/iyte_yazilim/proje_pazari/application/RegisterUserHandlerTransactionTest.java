package com.iyte_yazilim.proje_pazari.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import com.iyte_yazilim.proje_pazari.application.commands.registerUser.RegisterUserCommand;
import com.iyte_yazilim.proje_pazari.application.commands.registerUser.RegisterUserHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.RegisterUserResult;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class RegisterUserHandlerTransactionTest {

    @Autowired private RegisterUserHandler handler;

    // When using @SpyBean, we use the real repository but
    // we can simulate exceptions for specific methods if needed.
    private final UserRepository userRepository = Mockito.mock(UserRepository.class);

    @Test
    void shouldRollbackTransaction_whenExceptionThrown() {
        // Given
        RegisterUserCommand command =
                new RegisterUserCommand(
                        "test-rollback@std.iyte.edu.tr", "password123", "Test", "User");

        long initialCount = userRepository.count();

        // Simulate exception thrown during database save
        doThrow(new DataIntegrityViolationException("Simulated DB Error"))
                .when(userRepository)
                .save(any());

        // When & Then
        // Assert that the handler propagates the exception
        assertThrows(
                DataIntegrityViolationException.class,
                () -> {
                    handler.handle(command);
                });

        // Verify: Count should not change since transaction should be rolled back
        assertEquals(
                initialCount,
                userRepository.count(),
                "Transaction rollback failed, data was persisted!");
    }

    @Test
    @Transactional // To prevent test data from polluting the database
    void shouldCommitTransaction_whenSuccessful() {
        // Given
        String email = "test-success@std.iyte.edu.tr";
        RegisterUserCommand command = new RegisterUserCommand(email, "password123", "Test", "User");

        // When
        ApiResponse<RegisterUserResult> response = handler.handle(command);

        // Then
        assertNotNull(response);
        assertEquals(201, response.getCode()); // Created
        assertTrue(userRepository.existsByEmail(email), "User was not persisted to the database!");
    }
}
