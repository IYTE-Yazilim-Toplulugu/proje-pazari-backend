package com.iyte_yazilim.proje_pazari.application.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.domain.exceptions.ProjectNotFoundException;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.PendingIndexRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.PendingIndexEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.PendingIndexStatus;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PendingIndexRetryServiceTest {

    @Mock private PendingIndexRepository pendingIndexRepository;
    @Mock private ElasticsearchSyncService syncService;

    @InjectMocks private PendingIndexRetryService retryService;

    private PendingIndexEntity pendingEntry(String projectId) {
        PendingIndexEntity entry = new PendingIndexEntity();
        entry.setProjectId(projectId);
        entry.setStatus(PendingIndexStatus.PENDING);
        entry.setAttemptCount(0);
        return entry;
    }

    @Test
    @DisplayName("Should do nothing when there are no pending entries")
    void retryPendingIndexes_noPendingEntries_doesNothing() {
        when(pendingIndexRepository.findByStatus(PendingIndexStatus.PENDING)).thenReturn(List.of());

        retryService.retryPendingIndexes();

        verifyNoInteractions(syncService);
        verify(pendingIndexRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should set status to DONE and save after successful retry")
    void retryPendingIndexes_successfulRetry_setsStatusDoneAndSaves() {
        PendingIndexEntity entry = pendingEntry("proj-1");
        when(pendingIndexRepository.findByStatus(PendingIndexStatus.PENDING))
                .thenReturn(List.of(entry));

        retryService.retryPendingIndexes();

        verify(syncService).indexProject("proj-1");
        assertEquals(PendingIndexStatus.DONE, entry.getStatus());
        verify(pendingIndexRepository).save(entry);
    }

    @Test
    @DisplayName("Should increment attemptCount and save when retry still fails")
    void retryPendingIndexes_retryFails_incrementsAttemptCountAndSaves() {
        PendingIndexEntity entry = pendingEntry("proj-2");
        doThrow(new RuntimeException("ES still down")).when(syncService).indexProject("proj-2");
        when(pendingIndexRepository.findByStatus(PendingIndexStatus.PENDING))
                .thenReturn(List.of(entry));

        retryService.retryPendingIndexes();

        assertEquals(PendingIndexStatus.PENDING, entry.getStatus());
        assertEquals(1, entry.getAttemptCount());
        verify(pendingIndexRepository).save(entry);
    }

    @Test
    @DisplayName("Should mark FAILED and drop stale index doc when project no longer exists")
    void retryPendingIndexes_projectMissing_marksFailedAndDeletesIndex() {
        PendingIndexEntity entry = pendingEntry("proj-gone");
        doThrow(new ProjectNotFoundException("proj-gone"))
                .when(syncService)
                .indexProject("proj-gone");
        when(pendingIndexRepository.findByStatus(PendingIndexStatus.PENDING))
                .thenReturn(List.of(entry));

        retryService.retryPendingIndexes();

        assertEquals(PendingIndexStatus.FAILED, entry.getStatus());
        assertEquals(0, entry.getAttemptCount());
        verify(syncService).deleteProjectIndex("proj-gone");
        verify(pendingIndexRepository).save(entry);
    }

    @Test
    @DisplayName("Should mark FAILED once attemptCount reaches the max attempt cap")
    void retryPendingIndexes_maxAttemptsReached_marksFailed() {
        PendingIndexEntity entry = pendingEntry("proj-poison");
        entry.setAttemptCount(PendingIndexRetryService.MAX_ATTEMPTS - 1);
        doThrow(new RuntimeException("ES down")).when(syncService).indexProject("proj-poison");
        when(pendingIndexRepository.findByStatus(PendingIndexStatus.PENDING))
                .thenReturn(List.of(entry));

        retryService.retryPendingIndexes();

        assertEquals(PendingIndexRetryService.MAX_ATTEMPTS, entry.getAttemptCount());
        assertEquals(PendingIndexStatus.FAILED, entry.getStatus());
        verify(pendingIndexRepository).save(entry);
    }

    @Test
    @DisplayName("Should not throw when a retry fails — loop continues for remaining entries")
    void retryPendingIndexes_oneEntryFails_continuesWithRemaining() {
        PendingIndexEntity failing = pendingEntry("proj-fail");
        PendingIndexEntity succeeding = pendingEntry("proj-ok");
        doThrow(new RuntimeException("ES down")).when(syncService).indexProject("proj-fail");
        when(pendingIndexRepository.findByStatus(PendingIndexStatus.PENDING))
                .thenReturn(List.of(failing, succeeding));

        assertDoesNotThrow(() -> retryService.retryPendingIndexes());

        verify(syncService).indexProject("proj-fail");
        verify(syncService).indexProject("proj-ok");
        assertEquals(PendingIndexStatus.PENDING, failing.getStatus());
        assertEquals(PendingIndexStatus.DONE, succeeding.getStatus());
        verify(pendingIndexRepository).save(failing);
        verify(pendingIndexRepository).save(succeeding);
    }

    @Test
    @DisplayName("Should process all pending entries independently")
    void retryPendingIndexes_multipleEntries_allProcessed() {
        PendingIndexEntity e1 = pendingEntry("proj-1");
        PendingIndexEntity e2 = pendingEntry("proj-2");
        when(pendingIndexRepository.findByStatus(PendingIndexStatus.PENDING))
                .thenReturn(List.of(e1, e2));

        retryService.retryPendingIndexes();

        verify(syncService).indexProject("proj-1");
        verify(syncService).indexProject("proj-2");
        assertEquals(PendingIndexStatus.DONE, e1.getStatus());
        assertEquals(PendingIndexStatus.DONE, e2.getStatus());
        verify(pendingIndexRepository).save(e1);
        verify(pendingIndexRepository).save(e2);
    }

    @Test
    @DisplayName("Should accumulate attemptCount across multiple failed retries")
    void retryPendingIndexes_repeatedFailures_accumulatesAttemptCount() {
        PendingIndexEntity entry = pendingEntry("proj-3");
        entry.setAttemptCount(2);
        doThrow(new RuntimeException("ES down")).when(syncService).indexProject("proj-3");
        when(pendingIndexRepository.findByStatus(PendingIndexStatus.PENDING))
                .thenReturn(List.of(entry));

        retryService.retryPendingIndexes();

        assertEquals(3, entry.getAttemptCount());
        assertEquals(PendingIndexStatus.PENDING, entry.getStatus());
    }
}
