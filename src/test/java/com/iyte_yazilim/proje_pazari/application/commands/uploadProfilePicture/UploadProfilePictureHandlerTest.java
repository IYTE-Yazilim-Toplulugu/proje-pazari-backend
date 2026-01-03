package com.iyte_yazilim.proje_pazari.application.commands.uploadProfilePicture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.services.FileStorageService;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.io.IOException;
import java.util.Optional;
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

    @Mock private MultipartFile mockFile;

    @InjectMocks private UploadProfilePictureHandler handler;

    @Test
    @DisplayName("Should upload profile picture successfully when user has no existing picture")
    void shouldUploadPicture_whenNoExistingPicture() throws IOException {
        // Given
        String userId = Ulid.fast().toString();
        String fileName = userId + "_1234567890.jpg";
        UploadProfilePictureCommand command = new UploadProfilePictureCommand(userId, mockFile);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setProfilePictureUrl(null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(fileStorageService.storeFile(mockFile, userId)).thenReturn(fileName);

        // When
        ApiResponse<String> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("Profile picture uploaded successfully", response.getMessage());
        assertEquals("/api/v1/files/" + fileName, response.getData());
        assertEquals("/api/v1/files/" + fileName, userEntity.getProfilePictureUrl());
        verify(userRepository).save(userEntity);
        verify(fileStorageService, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("Should upload profile picture and delete old one when user has existing picture")
    void shouldUploadPicture_whenExistingPictureExists() throws IOException {
        // Given
        String userId = Ulid.fast().toString();
        String oldFileName = userId + "_old.jpg";
        String newFileName = userId + "_new.jpg";
        UploadProfilePictureCommand command = new UploadProfilePictureCommand(userId, mockFile);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setProfilePictureUrl("/api/v1/files/" + oldFileName);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(fileStorageService.storeFile(mockFile, userId)).thenReturn(newFileName);

        // When
        ApiResponse<String> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("/api/v1/files/" + newFileName, response.getData());
        verify(fileStorageService).deleteFile(oldFileName);
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
    void shouldReturnError_whenFileTypeIsInvalid() throws IOException {
        // Given
        String userId = Ulid.fast().toString();
        UploadProfilePictureCommand command = new UploadProfilePictureCommand(userId, mockFile);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(fileStorageService.storeFile(mockFile, userId))
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
    void shouldReturnError_whenFileSizeExceedsLimit() throws IOException {
        // Given
        String userId = Ulid.fast().toString();
        UploadProfilePictureCommand command = new UploadProfilePictureCommand(userId, mockFile);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(fileStorageService.storeFile(mockFile, userId))
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
    void shouldReturnError_whenStorageFails() throws IOException {
        // Given
        String userId = Ulid.fast().toString();
        UploadProfilePictureCommand command = new UploadProfilePictureCommand(userId, mockFile);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(fileStorageService.storeFile(mockFile, userId))
                .thenThrow(new IOException("Disk full"));

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
    void shouldContinue_whenDeleteOldFileFails() throws IOException {
        // Given
        String userId = Ulid.fast().toString();
        String oldFileName = userId + "_old.jpg";
        String newFileName = userId + "_new.jpg";
        UploadProfilePictureCommand command = new UploadProfilePictureCommand(userId, mockFile);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setProfilePictureUrl("/api/v1/files/" + oldFileName);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        doThrow(new IOException("File not found")).when(fileStorageService).deleteFile(oldFileName);
        when(fileStorageService.storeFile(mockFile, userId)).thenReturn(newFileName);

        // When
        ApiResponse<String> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("/api/v1/files/" + newFileName, response.getData());
        verify(userRepository).save(userEntity);
    }

    @Test
    @DisplayName("Should not delete when extracted filename contains path traversal characters")
    void shouldNotDelete_whenFilenameContainsPathTraversal() throws IOException {
        // Given
        String userId = Ulid.fast().toString();
        String newFileName = userId + "_new.jpg";
        UploadProfilePictureCommand command = new UploadProfilePictureCommand(userId, mockFile);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        // The handler extracts filename after last "/" - this results in "..passwd"
        userEntity.setProfilePictureUrl("/api/v1/files/..passwd");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(fileStorageService.storeFile(mockFile, userId)).thenReturn(newFileName);

        // When
        ApiResponse<String> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        verify(fileStorageService, never()).deleteFile(anyString());
        verify(userRepository).save(userEntity);
    }

    @Test
    @DisplayName("Should not delete when old URL is blank")
    void shouldNotDelete_whenOldUrlIsBlank() throws IOException {
        // Given
        String userId = Ulid.fast().toString();
        String newFileName = userId + "_new.jpg";
        UploadProfilePictureCommand command = new UploadProfilePictureCommand(userId, mockFile);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setProfilePictureUrl("   ");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(fileStorageService.storeFile(mockFile, userId)).thenReturn(newFileName);

        // When
        ApiResponse<String> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        verify(fileStorageService, never()).deleteFile(anyString());
        verify(userRepository).save(userEntity);
    }
}
