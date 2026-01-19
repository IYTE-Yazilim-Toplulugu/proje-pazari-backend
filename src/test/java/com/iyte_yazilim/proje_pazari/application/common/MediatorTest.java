package com.iyte_yazilim.proje_pazari.application.common;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.behaviors.IPipelineBehavior;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class MediatorTest {

    @Mock private ApplicationContext applicationContext;

    private Mediator mediator;

    // Test request and handler
    record TestCommand(String data) implements IRequest<String> {}

    record TestQuery(int id) implements IRequest<Integer> {}

    static class TestCommandHandler implements IRequestHandler<TestCommand, String> {
        @Override
        public String handle(TestCommand request) {
            return "Handled: " + request.data();
        }
    }

    static class TestQueryHandler implements IRequestHandler<TestQuery, Integer> {
        @Override
        public Integer handle(TestQuery request) {
            return request.id() * 2;
        }
    }

    @BeforeEach
    void setUp() {
        mediator = new Mediator(applicationContext, new ArrayList<>());
    }

    @Test
    void send_shouldResolveHandlerAndExecute() {
        // Arrange
        TestCommand command = new TestCommand("test data");
        TestCommandHandler handler = new TestCommandHandler();

        Map<String, IRequestHandler> handlers = new HashMap<>();
        handlers.put("testCommandHandler", handler);

        when(applicationContext.getBeansOfType(IRequestHandler.class)).thenReturn(handlers);

        // Act
        String result = mediator.send(command);

        // Assert
        assertEquals("Handled: test data", result);
        verify(applicationContext).getBeansOfType(IRequestHandler.class);
    }

    @Test
    void send_shouldThrowExceptionWhenNoHandlerFound() {
        // Arrange
        TestCommand command = new TestCommand("test");
        when(applicationContext.getBeansOfType(IRequestHandler.class)).thenReturn(new HashMap<>());

        // Act & Assert
        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> mediator.send(command));

        assertTrue(exception.getMessage().contains("No handler found for request"));
    }

    @Test
    void send_shouldResolveCorrectHandlerForDifferentRequestTypes() {
        // Arrange
        TestCommand command = new TestCommand("data");
        TestQuery query = new TestQuery(5);

        TestCommandHandler commandHandler = new TestCommandHandler();
        TestQueryHandler queryHandler = new TestQueryHandler();

        Map<String, IRequestHandler> handlers = new HashMap<>();
        handlers.put("testCommandHandler", commandHandler);
        handlers.put("testQueryHandler", queryHandler);

        when(applicationContext.getBeansOfType(IRequestHandler.class)).thenReturn(handlers);

        // Act
        String commandResult = mediator.send(command);
        Integer queryResult = mediator.send(query);

        // Assert
        assertEquals("Handled: data", commandResult);
        assertEquals(10, queryResult);
    }

    @Test
    void send_shouldExecutePipelineBehaviors() {
        // Arrange
        TestCommand command = new TestCommand("test");
        TestCommandHandler handler = new TestCommandHandler();

        Map<String, IRequestHandler> handlers = new HashMap<>();
        handlers.put("testCommandHandler", handler);

        List<String> executionOrder = new ArrayList<>();

        IPipelineBehavior<IRequest<Object>, Object> behavior1 =
                (request, next) -> {
                    executionOrder.add("behavior1-before");
                    Object result = next.handle();
                    executionOrder.add("behavior1-after");
                    return result;
                };

        IPipelineBehavior<IRequest<Object>, Object> behavior2 =
                (request, next) -> {
                    executionOrder.add("behavior2-before");
                    Object result = next.handle();
                    executionOrder.add("behavior2-after");
                    return result;
                };

        List<IPipelineBehavior<?, ?>> behaviors = new ArrayList<>();
        behaviors.add(behavior1);
        behaviors.add(behavior2);

        Mediator mediatorWithBehaviors = new Mediator(applicationContext, behaviors);

        when(applicationContext.getBeansOfType(IRequestHandler.class)).thenReturn(handlers);

        // Act
        String result = mediatorWithBehaviors.send(command);

        // Assert
        assertEquals("Handled: test", result);
        assertEquals(4, executionOrder.size());
        assertEquals("behavior1-before", executionOrder.get(0));
        assertEquals("behavior2-before", executionOrder.get(1));
        assertEquals("behavior2-after", executionOrder.get(2));
        assertEquals("behavior1-after", executionOrder.get(3));
    }

    @Test
    void send_shouldWorkWithNullBehaviorsList() {
        // Arrange
        Mediator mediatorNullBehaviors = new Mediator(applicationContext, null);
        TestCommand command = new TestCommand("test");
        TestCommandHandler handler = new TestCommandHandler();

        Map<String, IRequestHandler> handlers = new HashMap<>();
        handlers.put("testCommandHandler", handler);

        when(applicationContext.getBeansOfType(IRequestHandler.class)).thenReturn(handlers);

        // Act
        String result = mediatorNullBehaviors.send(command);

        // Assert
        assertEquals("Handled: test", result);
    }

    @Test
    void send_shouldMeasurePerformanceOverhead() {
        // Arrange
        TestCommand command = new TestCommand("perf test");
        TestCommandHandler handler = new TestCommandHandler();

        Map<String, IRequestHandler> handlers = new HashMap<>();
        handlers.put("testCommandHandler", handler);

        when(applicationContext.getBeansOfType(IRequestHandler.class)).thenReturn(handlers);

        // Act - measure multiple executions
        long startTime = System.nanoTime();
        int iterations = 1000;
        for (int i = 0; i < iterations; i++) {
            mediator.send(command);
        }
        long endTime = System.nanoTime();

        double averageMs = (endTime - startTime) / 1_000_000.0 / iterations;

        // Assert - mediator overhead should be less than 5ms per request
        assertTrue(averageMs < 5, "Mediator overhead should be less than 5ms, was: " + averageMs);
    }
}
