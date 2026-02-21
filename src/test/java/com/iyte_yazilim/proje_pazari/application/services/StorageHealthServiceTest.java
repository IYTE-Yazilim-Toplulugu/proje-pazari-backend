package com.iyte_yazilim.proje_pazari.application.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.iyte_yazilim.proje_pazari.application.dtos.StorageHealthDTO;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IFileStorageAdapter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StorageHealthServiceTest {

    @Mock private IFileStorageAdapter storageAdapter;

    private StorageHealthService storageHealthService;

    @BeforeEach
    void setUp() {
        storageHealthService = new StorageHealthService(storageAdapter);
    }

    @Test
    void shouldReturnStorageHealthWhenAdapterIsReachable() {
        when(storageAdapter.isAvailable()).thenReturn(true);
        when(storageAdapter.getTotalSpaceBytes()).thenReturn(1_000_000L);
        when(storageAdapter.getUsedSpaceBytes()).thenReturn(250_000L);
        when(storageAdapter.listBuckets()).thenReturn(List.of("proje-pazari-files"));

        StorageHealthDTO health = storageHealthService.getStorageHealth();

        assertTrue(health.available());
        assertEquals(1_000_000L, health.totalSpaceBytes());
        assertEquals(250_000L, health.usedSpaceBytes());
        assertEquals(List.of("proje-pazari-files"), health.buckets());
        assertNotNull(health.provider());
        assertNotNull(health.checkedAt());
    }

    @Test
    void shouldReturnUnavailableWhenAdapterThrows() {
        when(storageAdapter.isAvailable()).thenThrow(new RuntimeException("storage error"));

        StorageHealthDTO health = storageHealthService.getStorageHealth();

        assertFalse(health.available());
        assertNull(health.totalSpaceBytes());
        assertNull(health.usedSpaceBytes());
        assertTrue(health.buckets().isEmpty());
        assertNotNull(health.checkedAt());
    }
}
