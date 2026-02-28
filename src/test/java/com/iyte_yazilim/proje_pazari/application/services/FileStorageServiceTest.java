package com.iyte_yazilim.proje_pazari.application.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

class FileStorageServiceTest {

    @TempDir Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(tempDir);
    }

    @Test
    @DisplayName("Should store image file successfully")
    void shouldStoreFile_whenFileIsValidImage() throws IOException {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getOriginalFilename()).thenReturn("photo.jpg");
        when(file.getSize()).thenReturn(1024L);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[] {1, 2, 3}));

        // When
        String fileName = fileStorageService.storeFile(file, "user-123");

        // Then
        assertNotNull(fileName);
        assertTrue(fileName.startsWith("user-123_"));
        assertTrue(fileName.endsWith(".jpg"));
        assertTrue(Files.exists(tempDir.resolve(fileName)));
    }

    @Test
    @DisplayName("Should reject non-image file")
    void shouldRejectFile_whenNotImage() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getOriginalFilename()).thenReturn("document.pdf");

        // When & Then
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> fileStorageService.storeFile(file, "user-123"));
        assertEquals("Only image files are allowed", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject file exceeding size limit")
    void shouldRejectFile_whenSizeExceedsLimit() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getOriginalFilename()).thenReturn("large.png");
        when(file.getSize()).thenReturn(6L * 1024 * 1024); // 6MB

        // When & Then
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> fileStorageService.storeFile(file, "user-123"));
        assertEquals("File size exceeds 5MB limit", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject file with null filename")
    void shouldRejectFile_whenFilenameIsNull() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getOriginalFilename()).thenReturn(null);
        when(file.getSize()).thenReturn(1024L);

        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> fileStorageService.storeFile(file, "user-123"));
    }

    @Test
    @DisplayName("Should load existing file as resource")
    void shouldLoadFile_whenFileExists() throws IOException {
        // Given
        Path testFile = tempDir.resolve("test-file.jpg");
        Files.write(testFile, new byte[] {1, 2, 3});

        // When
        Resource resource = fileStorageService.loadFileAsResource("test-file.jpg");

        // Then
        assertNotNull(resource);
        assertTrue(resource.exists());
    }

    @Test
    @DisplayName("Should throw when trying to load non-existent file")
    void shouldThrow_whenFileDoesNotExist() {
        // When & Then
        assertThrows(
                IOException.class, () -> fileStorageService.loadFileAsResource("nonexistent.jpg"));
    }

    @Test
    @DisplayName("Should delete existing file")
    void shouldDeleteFile_whenFileExists() throws IOException {
        // Given
        Path testFile = tempDir.resolve("to-delete.jpg");
        Files.write(testFile, new byte[] {1, 2, 3});

        // When
        fileStorageService.deleteFile("to-delete.jpg");

        // Then
        assertFalse(Files.exists(testFile));
    }

    @Test
    @DisplayName("Should not throw when deleting non-existent file")
    void shouldNotThrow_whenDeletingNonExistentFile() {
        // When & Then
        assertDoesNotThrow(() -> fileStorageService.deleteFile("nonexistent.jpg"));
    }

    @Test
    @DisplayName("Should reject file with image content type but wrong extension")
    void shouldRejectFile_whenExtensionIsInvalid() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getOriginalFilename()).thenReturn("malicious.exe");

        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> fileStorageService.storeFile(file, "user-123"));
    }
}
