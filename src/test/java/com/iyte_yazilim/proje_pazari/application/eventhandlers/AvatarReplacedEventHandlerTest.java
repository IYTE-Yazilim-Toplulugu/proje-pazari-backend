package com.iyte_yazilim.proje_pazari.application.eventhandlers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.services.FileStorageService;
import com.iyte_yazilim.proje_pazari.domain.events.AvatarReplacedEvent;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AvatarReplacedEventHandlerTest {

    @Mock private FileStorageService fileStorageService;

    @InjectMocks private AvatarReplacedEventHandler handler;

    @Test
    @DisplayName("Should delete the previous avatar object when the resolved paths differ")
    void shouldDeleteOldAvatar_whenPathsDiffer() {
        AvatarReplacedEvent event =
                new AvatarReplacedEvent(
                        "user-1",
                        "/api/v1/files/proje-pazari-avatars/users/user-1/avatar-old.jpg",
                        "proje-pazari-avatars/users/user-1/avatar-new.jpg",
                        LocalDateTime.now());

        handler.handle(event);

        verify(fileStorageService).deleteFile("proje-pazari-avatars/users/user-1/avatar-old.jpg");
    }

    @Test
    @DisplayName("Should skip cleanup when the normalized old and new paths are equal")
    void shouldSkipCleanup_whenPathsAreEqual() {
        String samePath = "proje-pazari-avatars/users/user-1/avatar-same.jpg";
        AvatarReplacedEvent event =
                new AvatarReplacedEvent("user-1", samePath, samePath, LocalDateTime.now());

        handler.handle(event);

        verify(fileStorageService, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("Should skip cleanup when the previous stored value has no resolvable path")
    void shouldSkipCleanup_whenOldPathUnresolvable() {
        AvatarReplacedEvent event =
                new AvatarReplacedEvent(
                        "user-1",
                        "http://minio:9000",
                        "proje-pazari-avatars/users/user-1/avatar-new.jpg",
                        LocalDateTime.now());

        handler.handle(event);

        verify(fileStorageService, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("Should swallow the failure and not propagate when deletion throws")
    void shouldSwallowFailure_whenDeleteThrows() {
        String oldPath = "proje-pazari-avatars/users/user-1/avatar-old.jpg";
        AvatarReplacedEvent event =
                new AvatarReplacedEvent(
                        "user-1",
                        oldPath,
                        "proje-pazari-avatars/users/user-1/avatar-new.jpg",
                        LocalDateTime.now());
        doThrow(new FileStorageException("object already gone"))
                .when(fileStorageService)
                .deleteFile(oldPath);

        handler.handle(event);

        verify(fileStorageService).deleteFile(oldPath);
    }
}
