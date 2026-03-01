package com.iyte_yazilim.proje_pazari.application.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IFileStorageAdapter;
import com.iyte_yazilim.proje_pazari.domain.models.FileMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @Mock
    private IFileStorageAdapter storageAdapter;

    @InjectMocks
    private FileStorageService fileStorageService;

    private void setDefaultConfig() {
        ReflectionTestUtils.setField(fileStorageService, "maxFileSize", DataSize.ofMegabytes(10));
        ReflectionTestUtils.setField(
                fileStorageService,
                "allowedContentTypesString",
                "image/jpeg,image/png,image/gif,image/webp,application/pdf");
    }

    @Test
    @DisplayName("Should store file successfully")
    void shouldStoreFile_whenFileIsValid() {
        // Given
        setDefaultConfig();
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getOriginalFilename()).thenReturn("photo.jpg");
        when(file.getSize()).thenReturn(1024L);
        when(file.isEmpty()).thenReturn(false);
        when(storageAdapter.store(any(MultipartFile.class), anyString()))
                .thenReturn("https://storage.example.com/profiles/photo.jpg");

        // When
        String url = fileStorageService.storeFile(file, "profiles");

        // Then
        assertNotNull(url);
        assertTrue(url.contains("storage.example.com"));
        verify(storageAdapter).store(any(MultipartFile.class), anyString());
    }

    @Test
    @DisplayName("Should reject empty file")
    void shouldRejectFile_whenEmpty() {
        // Given
        setDefaultConfig();
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        // When & Then
        assertThrows(
                FileStorageException.class, () -> fileStorageService.storeFile(file, "profiles"));
    }

    @Test
    @DisplayName("Should reject file exceeding size limit")
    void shouldRejectFile_whenSizeExceedsLimit() {
        // Given
        setDefaultConfig();
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(20L * 1024 * 1024); // 20MB exceeds 10MB limit

        // When & Then
        assertThrows(
                FileStorageException.class, () -> fileStorageService.storeFile(file, "profiles"));
    }

    @Test
    @DisplayName("Should reject file with disallowed content type")
    void shouldRejectFile_whenContentTypeNotAllowed() {
        // Given
        setDefaultConfig();
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("application/zip");

        // When & Then
        assertThrows(
                FileStorageException.class, () -> fileStorageService.storeFile(file, "profiles"));
    }

    @Test
    @DisplayName("Should delete file")
    void shouldDeleteFile() {
        // Given
        String path = "profiles/photo.jpg";

        // When
        fileStorageService.deleteFile(path);

        // Then
        verify(storageAdapter).delete(path);
    }

    @Test
    @DisplayName("Should check if file exists")
    void shouldCheckFileExists() {
        // Given
        String path = "profiles/photo.jpg";
        when(storageAdapter.exists(path)).thenReturn(true);

        // When
        boolean exists = fileStorageService.fileExists(path);

        // Then
        assertTrue(exists);
        verify(storageAdapter).exists(path);
    }

    @Test
    @DisplayName("Should get file metadata")
    void shouldGetFileMetadata() {
        // Given
        String path = "profiles/photo.jpg";
        FileMetadata expectedMetadata = mock(FileMetadata.class);
        when(storageAdapter.getMetadata(path)).thenReturn(expectedMetadata);

        // When
        FileMetadata metadata = fileStorageService.getFileMetadata(path);

        // Then
        assertNotNull(metadata);
        assertEquals(expectedMetadata, metadata);
        verify(storageAdapter).getMetadata(path);
    }

    @Test
    @DisplayName("Should reject invalid file path with directory traversal")
    void shouldRejectInvalidPath() {
        // When & Then
        assertThrows(
                FileStorageException.class,
                () -> fileStorageService.deleteFile("../etc/passwd"));
    }

    @Test
    @DisplayName("Should generate presigned URL")
    void shouldGeneratePresignedUrl() {
        // Given
        String path = "profiles/photo.jpg";
        String expectedUrl = "https://storage.example.com/presigned/photo.jpg";
        when(storageAdapter.generatePresignedUrl(path, 60)).thenReturn(expectedUrl);

        // When
        String url = fileStorageService.getFileUrl(path);

        // Then
        assertEquals(expectedUrl, url);
        verify(storageAdapter).generatePresignedUrl(path, 60);
    }
}
