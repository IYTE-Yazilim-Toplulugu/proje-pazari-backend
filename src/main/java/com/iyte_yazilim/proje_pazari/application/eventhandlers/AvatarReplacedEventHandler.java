package com.iyte_yazilim.proje_pazari.application.eventhandlers;

import com.iyte_yazilim.proje_pazari.application.commands.uploadProfilePicture.AvatarPathResolver;
import com.iyte_yazilim.proje_pazari.application.services.FileStorageService;
import com.iyte_yazilim.proje_pazari.domain.events.AvatarReplacedEvent;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Deletes the avatar object a replacement upload superseded, once the replacement's database
 * reference has actually committed.
 *
 * <p>Running only after commit means a transaction that never commits leaves the previous object
 * untouched, matching the still-unchanged database value. Cleanup failure here is logged but never
 * propagated: the upload has already succeeded from the client's perspective, and an orphaned old
 * object is a leak to clean up later, not a reason to fail an already-committed response.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AvatarReplacedEventHandler implements IEventHandler<AvatarReplacedEvent> {

    private final FileStorageService fileStorageService;

    @Async
    @Override
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AvatarReplacedEvent event) {
        String oldPath = AvatarPathResolver.resolveStoragePath(event.previousStoredValue());
        String newPath = AvatarPathResolver.resolveStoragePath(event.newStoredValue());

        if (oldPath == null || oldPath.equals(newPath)) {
            return;
        }

        try {
            fileStorageService.deleteFile(oldPath);
            log.info("Deleted previous avatar object for user {}: {}", event.userId(), oldPath);
        } catch (Exception e) {
            log.warn(
                    "Failed to clean up previous avatar object for user {} [{}]: {}",
                    event.userId(),
                    oldPath,
                    e.getMessage(),
                    e);
        }
    }
}
