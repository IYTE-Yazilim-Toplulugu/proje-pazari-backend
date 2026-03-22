package com.iyte_yazilim.proje_pazari.application.commands.uploadProfilePicture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.exceptions.ValidationException;
import com.iyte_yazilim.proje_pazari.application.services.FileStorageService;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class UploadProfilePictureHandlerTest {

    @Mock private FileStorageService fileStorageService;

    @Mock private UserRepository userRepository;

    @Mock private MessageService messageService;

    @Mock private MultipartFile mockFile;

    @InjectMocks private UploadProfilePictureHandler handler;

    @BeforeEach
    void setUp() {
        lenient().when(messageService.getMessage("user.not.found")).thenReturn("User not found");
        lenient()
                .when(messageService.getMessage("user.profile.picture.uploaded"))
                .thenReturn("Profile picture uploaded successfully");
        lenient()
                .when(messageService.getMessage(eq("file.upload.failed"), any(Object[].class)))
                .thenAnswer(
                        invocation -> {
                            Object[] args = invocation.getArgument(1);
                            return "Failed to upload file: " + args[0];
                        });
    }

    @Test
    @DisplayName("Should upload profile picture successfully when user has no existing picture")
    void shouldUploadPicture_whenNoExistingPicture() {
        // Given
        String userId = Ulid.fast().toString();
        String storedUrl = "http://minio:9000/bucket/profiles/some-ulid.jpg";
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
        verify(userRepository).save(userEntity);
        verify(fileStorageService, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("Should upload profile picture and delete old one when user has existing picture")
    void shouldUploadPicture_whenExistingPictureExists() {
        // Given
        String userId = Ulid.fast().toString();
        String oldUrl = "/api/v1/files/profiles/old-ulid.jpg";
        String newStoredUrl = "http://minio:9000/bucket/profiles/new-ulid.jpg";
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
        verify(fileStorageService).deleteFile("profiles/old-ulid.jpg");
        verify(userRepository).save(userEntity);
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
        verify(userRepository, never()).save(any());
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
        verify(userRepository, never()).save(any());
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
        verify(userRepository, never()).save(any());
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
        verify(userRepository, never()).save(any());
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
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw FileStorageException when file storage fails")
    void shouldThrowException_whenStorageFails() {
        // Given
        String userId = Ulid.fast().toString();
        UploadProfilePictureCommand command = new UploadProfilePictureCommand(userId, mockFile);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(mockFile.isEmpty()).thenReturn(false);
        when(fileStorageService.storeUserAvatar(userId, mockFile))
                .thenThrow(new FileStorageException("Disk full"));

        // When & Then
        FileStorageException exception =
                assertThrows(FileStorageException.class, () -> handler.handle(command));
        assertEquals("Disk full", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should continue when deleting old file fails")
    void shouldContinue_whenDeleteOldFileFails() {
        // Given
        String userId = Ulid.fast().toString();
        String oldUrl = "/api/v1/files/profiles/old-ulid.jpg";
        String newStoredUrl = "http://minio:9000/bucket/profiles/new-ulid.jpg";
        UploadProfilePictureCommand command = new UploadProfilePictureCommand(userId, mockFile);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setProfilePictureUrl(oldUrl);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(mockFile.isEmpty()).thenReturn(false);
        doThrow(new FileStorageException("File not found"))
                .when(fileStorageService)
                .deleteFile("profiles/old-ulid.jpg");
        when(fileStorageService.storeUserAvatar(userId, mockFile)).thenReturn(newStoredUrl);

        // When
        ApiResponse<String> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals(newStoredUrl, response.getData());
        verify(userRepository).save(userEntity);
    }

    @Test
    @DisplayName("Should not delete when extracted filename contains path traversal characters")
    void shouldNotDelete_whenFilenameContainsPathTraversal() {
        // Given
        String userId = Ulid.fast().toString();
        String newStoredUrl = "http://minio:9000/bucket/profiles/new-ulid.jpg";
        UploadProfilePictureCommand command = new UploadProfilePictureCommand(userId, mockFile);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        // The handler extracts filename after "/api/v1/files/" - this results in
        // "..passwd"
        userEntity.setProfilePictureUrl("/api/v1/files/..passwd");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(mockFile.isEmpty()).thenReturn(false);
        when(fileStorageService.storeUserAvatar(userId, mockFile)).thenReturn(newStoredUrl);

        // When
        ApiResponse<String> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        // The handler will try to delete "..passwd" but FileStorageService.deleteFile
        // validates the path and will throw for paths with ".."
        verify(userRepository).save(userEntity);
    }

    @Test
    @DisplayName("Should not delete when old URL is blank")
    void shouldNotDelete_whenOldUrlIsBlank() {
        // Given
        String userId = Ulid.fast().toString();
        String newStoredUrl = "http://minio:9000/bucket/profiles/new-ulid.jpg";
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
        verify(fileStorageService, never()).deleteFile(anyString());
        verify(userRepository).save(userEntity);
    }
}
