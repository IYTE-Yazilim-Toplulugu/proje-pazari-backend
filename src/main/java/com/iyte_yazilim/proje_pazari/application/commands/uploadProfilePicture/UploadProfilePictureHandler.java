package com.iyte_yazilim.proje_pazari.application.commands.uploadProfilePicture;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.exceptions.ValidationException;
import com.iyte_yazilim.proje_pazari.application.services.FileStorageService;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.events.AvatarReplacedEvent;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Replaces a user's avatar without ever leaving the database and storage backend inconsistent.
 *
 * <p>The replacement object is stored and persisted before the previous object is touched at all,
 * so a storage or persistence failure leaves the previous object and database value exactly as they
 * were. The previous object is only deleted after the new database value has committed, via {@link
 * AvatarReplacedEvent} / {@link
 * com.iyte_yazilim.proje_pazari.application.eventhandlers.AvatarReplacedEventHandler} — never
 * inline here, since an inline delete could not be rolled back if the commit that follows it fails.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UploadProfilePictureHandler
        implements IRequestHandler<UploadProfilePictureCommand, ApiResponse<String>> {

    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final MessageService messageService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional(
            timeoutString = "${spring.transaction.timeout:30}",
            rollbackFor = Exception.class,
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public ApiResponse<String> handle(UploadProfilePictureCommand command) {
        UserEntity user =
                userRepository
                        .findById(command.userId())
                        .orElseThrow(() -> new UserNotFoundException(command.userId()));

        if (command.file() == null || command.file().isEmpty()) {
            throw new ValidationException("File is required");
        }

        String oldUrl = user.getProfilePictureUrl();

        // Store the replacement under a new, distinct object key first. Nothing about the
        // previous object is touched yet, so a storage failure here leaves both the database
        // value and the previous object completely unchanged.
        String newUrl = fileStorageService.storeUserAvatar(command.userId(), command.file());

        user.setProfilePictureUrl(newUrl);
        try {
            // Flushed immediately rather than left for the transaction's eventual commit, so a
            // persistence failure surfaces here - while the newly stored object can still be
            // cleaned up - instead of silently after this method has already returned.
            userRepository.saveAndFlush(user);
        } catch (RuntimeException e) {
            cleanupBestEffort(newUrl);
            throw e;
        }

        // Deleting the previous object is deferred until after this transaction commits: if it
        // never commits, the previous object must still be there for the (unchanged) database
        // value.
        if (oldUrl != null && !oldUrl.isBlank()) {
            applicationEventPublisher.publishEvent(
                    new AvatarReplacedEvent(command.userId(), oldUrl, newUrl, LocalDateTime.now()));
        }

        return ApiResponse.success(
                user.getProfilePictureUrl(),
                messageService.getMessage("user.profile.picture.uploaded"));
    }

    /**
     * Best-effort cleanup of the object just stored, after the database write that was meant to
     * reference it failed. Never rethrows: the persistence failure being handled by the caller is
     * the one that must reach the client, not a secondary storage error from this cleanup.
     */
    private void cleanupBestEffort(String newUrl) {
        String newPath = AvatarPathResolver.resolveStoragePath(newUrl);
        if (newPath == null) {
            return;
        }
        try {
            fileStorageService.deleteFile(newPath);
        } catch (Exception cleanupException) {
            log.warn(
                    "Failed to clean up newly stored avatar object [{}] after a persistence"
                            + " failure: {}",
                    newPath,
                    cleanupException.getMessage(),
                    cleanupException);
        }
    }
}
