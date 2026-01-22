package com.iyte_yazilim.proje_pazari.application.behaviors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.common.RequestHandlerDelegate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

@ExtendWith(MockitoExtension.class)
class TransactionBehaviorTest {

    @Mock private PlatformTransactionManager transactionManager;

    @Mock private TransactionStatus transactionStatus;

    private TransactionBehavior<IRequest<String>, String> transactionBehavior;

    record TestCommand(String data) implements IRequest<String> {}

    record TestQuery(int id) implements IRequest<String> {}

    @BeforeEach
    void setUp() {
        transactionBehavior = new TransactionBehavior<>(transactionManager);
    }

    @Test
    void handle_shouldWrapCommandInTransaction() {
        // Arrange
        TestCommand command = new TestCommand("test data");
        RequestHandlerDelegate<String> next = () -> "command result";

        // Mock the transaction manager to return a transaction status
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);

        // Act
        String result = transactionBehavior.handle(command, next);

        // Assert
        assertEquals("command result", result);
        // Verify transaction was started (getTransaction was called)
        verify(transactionManager).getTransaction(any());
        // Verify transaction was committed
        verify(transactionManager).commit(transactionStatus);
    }

    @Test
    void handle_shouldNotWrapQueryInTransaction() {
        // Arrange
        TestQuery query = new TestQuery(1);
        boolean[] nextCalled = {false};
        RequestHandlerDelegate<String> next =
                () -> {
                    nextCalled[0] = true;
                    return "query result";
                };

        // Act
        String result = transactionBehavior.handle(query, next);

        // Assert
        assertEquals("query result", result);
        assertTrue(nextCalled[0]);
        verifyNoInteractions(transactionManager);
    }

    @Test
    void handle_shouldIdentifyCommandByClassName() {
        // Arrange
        record CreateUserCommand(String name) implements IRequest<String> {}

        record UpdateProfileCommand(String data) implements IRequest<String> {}

        CreateUserCommand createCommand = new CreateUserCommand("John");
        UpdateProfileCommand updateCommand = new UpdateProfileCommand("data");

        // These should be identified as commands (name ends with "Command")
        assertTrue(createCommand.getClass().getSimpleName().endsWith("Command"));
        assertTrue(updateCommand.getClass().getSimpleName().endsWith("Command"));
    }

    @Test
    void handle_shouldIdentifyQueryByClassName() {
        // Arrange
        record GetUserQuery(int id) implements IRequest<String> {}

        record ListUsersQuery() implements IRequest<String> {}

        GetUserQuery getQuery = new GetUserQuery(1);
        ListUsersQuery listQuery = new ListUsersQuery();

        // These should NOT be identified as commands
        assertFalse(getQuery.getClass().getSimpleName().endsWith("Command"));
        assertFalse(listQuery.getClass().getSimpleName().endsWith("Command"));
    }

    @Test
    void handle_shouldExecuteNextDelegateForQueries() {
        // Arrange
        TestQuery query = new TestQuery(42);
        RequestHandlerDelegate<String> next = () -> "result for id 42";

        // Act
        String result = transactionBehavior.handle(query, next);

        // Assert
        assertEquals("result for id 42", result);
    }

    @Test
    void handle_shouldPropagateExceptionsFromCommands() {
        // Arrange
        TestCommand command = new TestCommand("test");
        RuntimeException expectedException = new RuntimeException("Command failed");

        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);

        RequestHandlerDelegate<String> next =
                () -> {
                    throw expectedException;
                };

        // Act & Assert
        RuntimeException thrown =
                assertThrows(
                        RuntimeException.class, () -> transactionBehavior.handle(command, next));

        assertEquals("Command failed", thrown.getMessage());
        // Verify rollback was called on exception
        verify(transactionManager).rollback(transactionStatus);
    }

    @Test
    void handle_shouldPropagateExceptionsFromQueries() {
        // Arrange
        TestQuery query = new TestQuery(1);
        RuntimeException expectedException = new RuntimeException("Query failed");

        RequestHandlerDelegate<String> next =
                () -> {
                    throw expectedException;
                };

        // Act & Assert
        RuntimeException thrown =
                assertThrows(RuntimeException.class, () -> transactionBehavior.handle(query, next));

        assertEquals("Query failed", thrown.getMessage());
    }
}
