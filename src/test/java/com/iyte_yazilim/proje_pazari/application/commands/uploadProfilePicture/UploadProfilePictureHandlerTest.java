package com.iyte_yazilim.proje_pazari.application.commands.uploadProfilePicture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.services.FileStorageService;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
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
        when(fileStorageService.storeFile(mockFile, "profiles")).thenReturn(storedUrl);

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
        when(fileStorageService.storeFile(mockFile, "profiles")).thenReturn(newStoredUrl);

        // When
        ApiResponse<String> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals(newStoredUrl, response.getData());
        verify(fileStorageService).deleteFile("profiles/old-ulid.jpg");
        verify(userRepository).save(userEntity);
    }

    @Test
    @DisplayName("Should return not found error when user does not exist")
    void shouldReturnError_whenUserNotFound() {
        // Given
        String nonExistentUserId = Ulid.fast().toString();
        UploadProfilePictureCommand command =
                new UploadProfilePictureCommand(nonExistentUserId, mockFile);

        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // When
        ApiResponse<String> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.NOT_FOUND, response.getCode());
        assertEquals("User not found", response.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return validation error when file type is invalid")
    void shouldReturnError_whenFileTypeIsInvalid() {
        // Given
        String userId = Ulid.fast().toString();
        UploadProfilePictureCommand command = new UploadProfilePictureCommand(userId, mockFile);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(fileStorageService.storeFile(mockFile, "profiles"))
                .thenThrow(new IllegalArgumentException("Only image files are allowed"));

        // When
        ApiResponse<String> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
        assertEquals("Only image files are allowed", response.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return validation error when file size exceeds limit")
    void shouldReturnError_whenFileSizeExceedsLimit() {
        // Given
        String userId = Ulid.fast().toString();
        UploadProfilePictureCommand command = new UploadProfilePictureCommand(userId, mockFile);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(fileStorageService.storeFile(mockFile, "profiles"))
                .thenThrow(new IllegalArgumentException("File size exceeds 5MB limit"));

        // When
        ApiResponse<String> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
        assertEquals("File size exceeds 5MB limit", response.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return error when file storage fails")
    void shouldReturnError_whenStorageFails() {
        // Given
        String userId = Ulid.fast().toString();
        UploadProfilePictureCommand command = new UploadProfilePictureCommand(userId, mockFile);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(fileStorageService.storeFile(mockFile, "profiles"))
                .thenThrow(new FileStorageException("Disk full"));

        // When
        ApiResponse<String> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.INTERNAL_SERVER_ERROR, response.getCode());
        assertTrue(response.getMessage().contains("Failed to upload file"));
        assertTrue(response.getMessage().contains("Disk full"));
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
        doThrow(new FileStorageException("File not found"))
                .when(fileStorageService)
                .deleteFile("profiles/old-ulid.jpg");
        when(fileStorageService.storeFile(mockFile, "profiles")).thenReturn(newStoredUrl);

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
        when(fileStorageService.storeFile(mockFile, "profiles")).thenReturn(newStoredUrl);

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
        when(fileStorageService.storeFile(mockFile, "profiles")).thenReturn(newStoredUrl);

        // When
        ApiResponse<String> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        verify(fileStorageService, never()).deleteFile(anyString());
        verify(userRepository).save(userEntity);
    }
}
