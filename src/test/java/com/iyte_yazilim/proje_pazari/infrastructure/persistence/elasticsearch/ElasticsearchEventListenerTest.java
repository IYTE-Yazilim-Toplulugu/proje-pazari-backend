package com.iyte_yazilim.proje_pazari.infrastructure.persistence.elasticsearch;

import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.services.ElasticsearchSyncService;
import com.iyte_yazilim.proje_pazari.domain.events.ProjectCreatedEvent;
import com.iyte_yazilim.proje_pazari.domain.events.ProjectDeletedEvent;
import com.iyte_yazilim.proje_pazari.domain.events.ProjectUpdatedEvent;
import com.iyte_yazilim.proje_pazari.infrastructure.metrics.BusinessMetricsService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ElasticsearchEventListenerTest {

    @Mock private ElasticsearchSyncService syncService;

    @Mock private BusinessMetricsService metricsService;

    @InjectMocks private ElasticsearchEventListener listener;

    // ── handleProjectCreated ──────────────────────────────────────────────

    @Test
    @DisplayName("Should index project and increment index success on project created")
    void shouldIndexProject_whenProjectCreated() throws Exception {
        // Given
        ProjectCreatedEvent event =
                new ProjectCreatedEvent(
                        "proj-1",
                        "Title",
                        "owner-1",
                        "owner@test.com",
                        "Owner",
                        LocalDateTime.now());

        // When
        listener.handleProjectCreated(event);

        // Then
        verify(syncService).indexProject("proj-1");
        verify(metricsService).incrementEsIndexSuccess();
        verify(metricsService, never()).incrementEsIndexFailure();
    }

    @Test
    @DisplayName("Should increment index failure when project created sync throws")
    void shouldIncrementIndexFailure_whenProjectCreatedSyncThrows() throws Exception {
        // Given
        ProjectCreatedEvent event =
                new ProjectCreatedEvent(
                        "proj-1",
                        "Title",
                        "owner-1",
                        "owner@test.com",
                        "Owner",
                        LocalDateTime.now());
        doThrow(new RuntimeException("ES unavailable")).when(syncService).indexProject("proj-1");

        // When
        listener.handleProjectCreated(event);

        // Then
        verify(metricsService).incrementEsIndexFailure();
        verify(metricsService, never()).incrementEsIndexSuccess();
    }

    // ── handleProjectUpdated ──────────────────────────────────────────────

    @Test
    @DisplayName("Should re-index project and increment index success on project updated")
    void shouldReindexProject_whenProjectUpdated() throws Exception {
        // Given
        ProjectUpdatedEvent event = new ProjectUpdatedEvent("proj-2");

        // When
        listener.handleProjectUpdated(event);

        // Then
        verify(syncService).indexProject("proj-2");
        verify(metricsService).incrementEsIndexSuccess();
        verify(metricsService, never()).incrementEsIndexFailure();
    }

    @Test
    @DisplayName("Should increment index failure when project updated sync throws")
    void shouldIncrementIndexFailure_whenProjectUpdatedSyncThrows() throws Exception {
        // Given
        ProjectUpdatedEvent event = new ProjectUpdatedEvent("proj-2");
        doThrow(new RuntimeException("ES unavailable")).when(syncService).indexProject("proj-2");

        // When
        listener.handleProjectUpdated(event);

        // Then
        verify(metricsService).incrementEsIndexFailure();
        verify(metricsService, never()).incrementEsIndexSuccess();
    }

    // ── handleProjectDeleted ──────────────────────────────────────────────

    @Test
    @DisplayName("Should delete project index and increment delete success on project deleted")
    void shouldDeleteProjectIndex_whenProjectDeleted() throws Exception {
        // Given
        ProjectDeletedEvent event =
                new ProjectDeletedEvent(
                        "proj-3",
                        "Test Project",
                        "owner-1",
                        "owner@test.com",
                        "Owner",
                        0,
                        List.of(),
                        List.of(),
                        LocalDateTime.now());

        // When
        listener.handleProjectDeleted(event);

        // Then
        verify(syncService).deleteProjectIndex("proj-3");
        verify(metricsService).incrementEsDeleteSuccess();
        verify(metricsService, never()).incrementEsDeleteFailure();
    }

    @Test
    @DisplayName("Should increment delete failure when project deleted sync throws")
    void shouldIncrementDeleteFailure_whenProjectDeletedSyncThrows() throws Exception {
        // Given
        ProjectDeletedEvent event =
                new ProjectDeletedEvent(
                        "proj-3",
                        "Test Project",
                        "owner-1",
                        "owner@test.com",
                        "Owner",
                        0,
                        List.of(),
                        List.of(),
                        LocalDateTime.now());
        doThrow(new RuntimeException("ES unavailable"))
                .when(syncService)
                .deleteProjectIndex("proj-3");

        // When
        listener.handleProjectDeleted(event);

        // Then
        verify(metricsService).incrementEsDeleteFailure();
        verify(metricsService, never()).incrementEsDeleteSuccess();
    }
}
