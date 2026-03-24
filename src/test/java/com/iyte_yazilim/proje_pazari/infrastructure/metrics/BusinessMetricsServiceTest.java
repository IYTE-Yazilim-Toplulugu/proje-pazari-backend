package com.iyte_yazilim.proje_pazari.infrastructure.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BusinessMetricsServiceTest {

    private SimpleMeterRegistry registry;
    private BusinessMetricsService metricsService;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metricsService = new BusinessMetricsService(registry);
    }

    private Counter findEsSyncCounter(String status, String operation) {
        return registry
                .find("elasticsearch.sync.total")
                .tags("status", status, "operation", operation)
                .counter();
    }

    @Test
    @DisplayName("Should increment ES index success counter")
    void shouldIncrementEsIndexSuccess() {
        // When
        metricsService.incrementEsIndexSuccess();

        // Then
        Counter counter = findEsSyncCounter("success", "index");
        assertEquals(1.0, counter.count());
    }

    @Test
    @DisplayName("Should increment ES index failure counter")
    void shouldIncrementEsIndexFailure() {
        // When
        metricsService.incrementEsIndexFailure();

        // Then
        Counter counter = findEsSyncCounter("failure", "index");
        assertEquals(1.0, counter.count());
    }

    @Test
    @DisplayName("Should increment ES delete success counter")
    void shouldIncrementEsDeleteSuccess() {
        // When
        metricsService.incrementEsDeleteSuccess();

        // Then
        Counter counter = findEsSyncCounter("success", "delete");
        assertEquals(1.0, counter.count());
    }

    @Test
    @DisplayName("Should increment ES delete failure counter")
    void shouldIncrementEsDeleteFailure() {
        // When
        metricsService.incrementEsDeleteFailure();

        // Then
        Counter counter = findEsSyncCounter("failure", "delete");
        assertEquals(1.0, counter.count());
    }

    @Test
    @DisplayName("Should keep index and delete counters independent")
    void shouldKeepIndexAndDeleteCountersIndependent() {
        // When
        metricsService.incrementEsIndexSuccess();
        metricsService.incrementEsIndexSuccess();
        metricsService.incrementEsDeleteSuccess();

        // Then
        assertEquals(2.0, findEsSyncCounter("success", "index").count());
        assertEquals(1.0, findEsSyncCounter("success", "delete").count());
    }
}
