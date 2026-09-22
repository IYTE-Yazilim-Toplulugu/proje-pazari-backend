package com.iyte_yazilim.proje_pazari.application.commands.uploadProfilePicture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ResponseCode;
import com.iyte_yazilim.proje_pazari.application.exceptions.ValidationException;
import com.iyte_yazilim.proje_pazari.application.services.FileStorageService;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.events.AvatarReplacedEvent;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class UploadProfilePictureHandlerTest {

    @Mock private FileStorageService fileStorageService;

    @Mock private UserRepository userRepository;

    @Mock private MessageService messageService;

    @Mock private ApplicationEventPublisher applicationEventPublisher;

    @Mock private MultipartFile mockFile;

    @InjectMocks private UploadProfilePictureHandler handler;

    @BeforeEach
    void setUp() {
        lenient().when(messageService.getMessage("user.not.found")).thenReturn("User not found");
        lenient()
                .when(messageService.getMessage("user.profile.picture.uploaded"))
                .thenReturn("Profile picture uploaded successfully");
    }

    @Test
    @DisplayName("Should upload profile picture successfully when user has no existing picture")
    void shouldUploadPicture_whenNoExistingPicture() {
        // Given
        String userId = Ulid.fast().toString();
        String storedUrl = "proje-pazari-avatars/users/" + userId + "/avatar-some-ulid.jpg";
        UploadProfilePictureCommand command = new UploadProfilePictureCommand(userId, mockFile);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setProfilePictureUrl(null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(mockFile.isEmpty()).thenReturn(false);
        when(fileStorageService.storeUserAvatar(userId, mockFile)).thenReturn(storedUrl);

        // When
        ApiResponse<String> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("Profile picture uploaded successfully", response.getMessage());
        assertEquals(storedUrl, response.getData());
        verify(userRepository).saveAndFlush(userEntity);
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Should publish AvatarReplacedEvent when user has an existing picture")
    void shouldPublishAvatarReplacedEvent_whenExistingPictureExists() {
        // Given
        String userId = Ulid.fast().toString();
        String oldUrl = "/api/v1/files/proje-pazari-avatars/users/" + userId + "/avatar-old.jpg";
        String newStoredUrl = "proje-pazari-avatars/users/" + userId + "/avatar-new.jpg";
        UploadProfilePictureCommand command = new UploadProfilePictureCommand(userId, mockFile);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setProfilePictureUrl(oldUrl);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(mockFile.isEmpty()).thenReturn(false);
        when(fileStorageService.storeUserAvatar(userId, mockFile)).thenReturn(newStoredUrl);

        // When
        ApiResponse<String> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals(newStoredUrl, response.getData());
        verify(userRepository).saveAndFlush(userEntity);
        verify(fileStorageService, never()).deleteFile(anyString());

        ArgumentCaptor<AvatarReplacedEvent> eventCaptor =
                ArgumentCaptor.forClass(AvatarReplacedEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        AvatarReplacedEvent published = eventCaptor.getValue();
        assertEquals(userId, published.userId());
        assertEquals(oldUrl, published.previousStoredValue());
        assertEquals(newStoredUrl, published.newStoredValue());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist")
    void shouldThrowException_whenUserNotFound() {
        // Given
        String nonExistentUserId = Ulid.fast().toString();
        UploadProfilePictureCommand command =
                new UploadProfilePictureCommand(nonExistentUserId, mockFile);

        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> handler.handle(command));
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when file is null")
    void shouldThrowException_whenFileIsNull() {
        // Given
        String userId = Ulid.fast().toString();
        UploadProfilePictureCommand command = new UploadProfilePictureCommand(userId, null);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        // When & Then
        ValidationException exception =
                assertThrows(ValidationException.class, () -> handler.handle(command));
        assertEquals("File is required", exception.getMessage());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when file is empty")
    void shouldThrowException_whenFileIsEmpty() {
        // Given
        String userId = Ulid.fast().toString();
        UploadProfilePictureCommand command = new UploadProfilePictureCommand(userId, mockFile);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(mockFile.isEmpty()).thenReturn(true);

        // When & Then
        ValidationException exception =
                assertThrows(ValidationException.class, () -> handler.handle(command));
        assertEquals("File is required", exception.getMessage());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when file type is invalid")
    void shouldThrowException_whenFileTypeIsInvalid() {
        // Given
        String userId = Ulid.fast().toString();
        UploadProfilePictureCommand command = new UploadProfilePictureCommand(userId, mockFile);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(mockFile.isEmpty()).thenReturn(false);
        when(fileStorageService.storeUserAvatar(userId, mockFile))
                .thenThrow(new IllegalArgumentException("Only image files are allowed"));

        // When & Then
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
        assertEquals("Only image files are allowed", exception.getMessage());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when file size exceeds limit")
    void shouldThrowException_whenFileSizeExceedsLimit() {
        // Given
        String userId = Ulid.fast().toString();
        UploadProfilePictureCommand command = new UploadProfilePictureCommand(userId, mockFile);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(mockFile.isEmpty()).thenReturn(false);
        when(fileStorageService.storeUserAvatar(userId, mockFile))
                .thenThrow(new IllegalArgumentException("File size exceeds 5MB limit"));

        // When & Then
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
        assertEquals("File size exceeds 5MB limit", exception.getMessage());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Should leave the old object and database value unchanged when storage fails")
    void shouldThrowException_whenStorageFails() {
        // Given
        String userId = Ulid.fast().toString();
        UploadProfilePictureCommand command = new UploadProfilePictureCommand(userId, mockFile);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setProfilePictureUrl("proje-pazari-avatars/users/" + userId + "/avatar-old.jpg");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(mockFile.isEmpty()).thenReturn(false);
        when(fileStorageService.storeUserAvatar(userId, mockFile))
                .thenThrow(new FileStorageException("Disk full"));

        // When & Then
        FileStorageException exception =
                assertThrows(FileStorageException.class, () -> handler.handle(command));
        assertEquals("Disk full", exception.getMessage());
        verify(userRepository, never()).saveAndFlush(any());
        verify(fileStorageService, never()).deleteFile(anyString());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Should clean up the newly stored object and rethrow when persistence fails")
    void shouldCleanUpNewObject_whenPersistenceFails() {
        // Given
        String userId = Ulid.fast().toString();
        String newStoredUrl = "proje-pazari-avatars/users/" + userId + "/avatar-new.jpg";
        UploadProfilePictureCommand command = new UploadProfilePictureCommand(userId, mockFile);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setProfilePictureUrl("proje-pazari-avatars/users/" + userId + "/avatar-old.jpg");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(mockFile.isEmpty()).thenReturn(false);
        when(fileStorageService.storeUserAvatar(userId, mockFile)).thenReturn(newStoredUrl);
        DataIntegrityViolationException persistenceFailure =
                new DataIntegrityViolationException("constraint violation");
        doThrow(persistenceFailure).when(userRepository).saveAndFlush(userEntity);

        // When & Then
        DataIntegrityViolationException thrown =
                assertThrows(DataIntegrityViolationException.class, () -> handler.handle(command));
        assertSame(persistenceFailure, thrown);
        verify(fileStorageService).deleteFile(newStoredUrl);
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Should not fail the upload when best-effort cleanup of the new object also fails")
    void shouldNotFail_whenBestEffortCleanupAlsoFails() {
        // Given
        String userId = Ulid.fast().toString();
        String newStoredUrl = "proje-pazari-avatars/users/" + userId + "/avatar-new.jpg";
        UploadProfilePictureCommand command = new UploadProfilePictureCommand(userId, mockFile);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(mockFile.isEmpty()).thenReturn(false);
        when(fileStorageService.storeUserAvatar(userId, mockFile)).thenReturn(newStoredUrl);
        DataIntegrityViolationException persistenceFailure =
                new DataIntegrityViolationException("constraint violation");
        doThrow(persistenceFailure).when(userRepository).saveAndFlush(userEntity);
        doThrow(new FileStorageException("cleanup also failed"))
                .when(fileStorageService)
                .deleteFile(newStoredUrl);

        // When & Then
        DataIntegrityViolationException thrown =
                assertThrows(DataIntegrityViolationException.class, () -> handler.handle(command));
        assertSame(persistenceFailure, thrown);
    }

    @Test
    @DisplayName("Should not publish a cleanup event when old profile picture URL is blank")
    void shouldNotPublishEvent_whenOldUrlIsBlank() {
        // Given
        String userId = Ulid.fast().toString();
        String newStoredUrl = "proje-pazari-avatars/users/" + userId + "/avatar-new.jpg";
        UploadProfilePictureCommand command = new UploadProfilePictureCommand(userId, mockFile);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setProfilePictureUrl("   ");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(mockFile.isEmpty()).thenReturn(false);
        when(fileStorageService.storeUserAvatar(userId, mockFile)).thenReturn(newStoredUrl);

        // When
        ApiResponse<String> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        verify(applicationEventPublisher, never()).publishEvent(any());
        verify(userRepository).saveAndFlush(userEntity);
    }
}
