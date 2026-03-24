package com.iyte_yazilim.proje_pazari.application.services;

import com.iyte_yazilim.proje_pazari.application.dtos.StorageHealthDTO;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IFileStorageAdapter;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StorageHealthService {

    private final IFileStorageAdapter storageAdapter;

    public StorageHealthDTO getStorageHealth() {
        boolean available;
        Long totalSpaceBytes = null;
        Long usedSpaceBytes = null;
        List<String> buckets = List.of();

        try {
            available = storageAdapter.isAvailable();
            totalSpaceBytes = storageAdapter.getTotalSpaceBytes();
            usedSpaceBytes = storageAdapter.getUsedSpaceBytes();
            buckets = storageAdapter.listBuckets();
        } catch (Exception e) {
            available = false;
        }

        return new StorageHealthDTO(
                storageAdapter.getClass().getSimpleName(),
                available,
                totalSpaceBytes,
                usedSpaceBytes,
                buckets,
                LocalDateTime.now());
    }
}
