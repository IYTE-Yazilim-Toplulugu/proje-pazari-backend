package com.iyte_yazilim.proje_pazari.application.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.domain.models.FileUpload;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FileValidationException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IFileStorageAdapter;
import com.iyte_yazilim.proje_pazari.domain.models.FileMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @Mock private IFileStorageAdapter storageAdapter;

    @InjectMocks private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileStorageService, "maxFileSize", DataSize.ofMegabytes(10));
        ReflectionTestUtils.setField(
                fileStorageService,
                "allowedContentTypesString",
                "image/jpeg,image/png,image/gif,image/webp,application/pdf");
        ReflectionTestUtils.setField(fileStorageService, "avatarsBucket", "proje-pazari-avatars");
        ReflectionTestUtils.setField(
                fileStorageService, "documentsBucket", "proje-pazari-documents");
    }

    @Test
    @DisplayName("Should store file successfully")
    void shouldStoreFile_whenFileIsValid() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getOriginalFilename()).thenReturn("photo.jpg");
        when(file.getSize()).thenReturn(1024L);
        when(file.isEmpty()).thenReturn(false);
        when(storageAdapter.store(any(FileUpload.class), anyString()))
                .thenReturn("https://storage.example.com/profiles/photo.jpg");

        // When
        String url = fileStorageService.storeFile(file, "profiles");

        // Then
        assertNotNull(url);
        assertTrue(url.contains("storage.example.com"));
        verify(storageAdapter).store(any(FileUpload.class), anyString());
    }

    @Test
    @DisplayName("Should reject empty file")
    void shouldRejectFile_whenEmpty() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        // When & Then
        assertThrows(
                FileValidationException.class,
                () -> fileStorageService.storeFile(file, "profiles"));
    }

    @Test
    @DisplayName("Should reject file exceeding size limit")
    void shouldRejectFile_whenSizeExceedsLimit() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(20L * 1024 * 1024); // 20MB exceeds 10MB limit

        // When & Then
        assertThrows(
                FileValidationException.class,
                () -> fileStorageService.storeFile(file, "profiles"));
    }

    @Test
    @DisplayName("Should reject file with disallowed content type")
    void shouldRejectFile_whenContentTypeNotAllowed() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("application/zip");

        // When & Then
        assertThrows(
                FileValidationException.class,
                () -> fileStorageService.storeFile(file, "profiles"));
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
                FileValidationException.class,
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

    @Test
    @DisplayName("Should reject executable file (.exe content type)")
    void shouldRejectFile_whenExecutableContentType() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("application/x-msdownload");

        // When & Then
        FileValidationException exception =
                assertThrows(
                        FileValidationException.class,
                        () -> fileStorageService.storeFile(file, "profiles"));
        assertTrue(exception.getMessage().contains("File type not allowed"));
    }

    @Test
    @DisplayName("Should reject shell script file (.sh content type)")
    void shouldRejectFile_whenShellScriptContentType() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("application/x-sh");

        // When & Then
        FileValidationException exception =
                assertThrows(
                        FileValidationException.class,
                        () -> fileStorageService.storeFile(file, "profiles"));
        assertTrue(exception.getMessage().contains("File type not allowed"));
    }

    @Test
    @DisplayName("Should reject octet-stream content type (generic binary)")
    void shouldRejectFile_whenOctetStreamContentType() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("application/octet-stream");

        // When & Then
        assertThrows(
                FileValidationException.class, () -> fileStorageService.storeFile(file, "uploads"));
    }

    @Test
    @DisplayName("Should reject file with null content type")
    void shouldRejectFile_whenContentTypeIsNull() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn(null);

        // When & Then
        assertThrows(
                FileValidationException.class,
                () -> fileStorageService.storeFile(file, "profiles"));
    }

    @Test
    @DisplayName("Should reject file at exact size limit boundary (10MB + 1 byte)")
    void shouldRejectFile_whenSizeExactlyExceedsLimit() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(10L * 1024 * 1024 + 1); // 10MB + 1 byte

        // When & Then
        FileValidationException exception =
                assertThrows(
                        FileValidationException.class,
                        () -> fileStorageService.storeFile(file, "profiles"));
        assertTrue(exception.getMessage().contains("exceeds the maximum allowed size"));
    }

    @Test
    @DisplayName("Should accept PDF file")
    void shouldAcceptFile_whenPdfContentType() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getOriginalFilename()).thenReturn("document.pdf");
        when(file.getSize()).thenReturn(5000L);
        when(file.isEmpty()).thenReturn(false);
        when(storageAdapter.store(any(FileUpload.class), anyString()))
                .thenReturn("https://storage.example.com/docs/document.pdf");

        // When
        String url = fileStorageService.storeFile(file, "docs");

        // Then
        assertNotNull(url);
        verify(storageAdapter).store(any(FileUpload.class), anyString());
    }

    @Test
    @DisplayName("Should accept WebP image file")
    void shouldAcceptFile_whenWebpContentType() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/webp");
        when(file.getOriginalFilename()).thenReturn("image.webp");
        when(file.getSize()).thenReturn(2048L);
        when(file.isEmpty()).thenReturn(false);
        when(storageAdapter.store(any(FileUpload.class), anyString()))
                .thenReturn("https://storage.example.com/profiles/image.webp");

        // When
        String url = fileStorageService.storeFile(file, "profiles");

        // Then
        assertNotNull(url);
        verify(storageAdapter).store(any(FileUpload.class), anyString());
    }

    @Test
    @DisplayName("Should accept GIF image file")
    void shouldAcceptFile_whenGifContentType() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/gif");
        when(file.getOriginalFilename()).thenReturn("animation.gif");
        when(file.getSize()).thenReturn(3072L);
        when(file.isEmpty()).thenReturn(false);
        when(storageAdapter.store(any(FileUpload.class), anyString()))
                .thenReturn("https://storage.example.com/profiles/animation.gif");

        // When
        String url = fileStorageService.storeFile(file, "profiles");

        // Then
        assertNotNull(url);
        verify(storageAdapter).store(any(FileUpload.class), anyString());
    }

    @Test
    @DisplayName("Should store file in correct directory path")
    void shouldStoreFile_inCorrectDirectory() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getOriginalFilename()).thenReturn("photo.jpg");
        when(file.getSize()).thenReturn(1024L);
        when(file.isEmpty()).thenReturn(false);
        when(storageAdapter.store(any(FileUpload.class), anyString())).thenReturn("stored-url");

        // When
        fileStorageService.storeFile(file, "projects/user123");

        // Then
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(storageAdapter).store(any(FileUpload.class), pathCaptor.capture());
        assertTrue(pathCaptor.getValue().startsWith("projects/user123/"));
        assertTrue(pathCaptor.getValue().endsWith(".jpg"));
    }

    @Test
    @DisplayName("Should reject path with leading slash")
    void shouldRejectPath_whenStartsWithSlash() {
        assertThrows(
                FileValidationException.class,
                () -> fileStorageService.deleteFile("/profiles/photo.jpg"));
    }

    @Test
    @DisplayName("Should reject path with backslash traversal")
    void shouldRejectPath_whenContainsBackslashTraversal() {
        assertThrows(
                FileValidationException.class,
                () -> fileStorageService.deleteFile("\\profiles\\photo.jpg"));
    }

    @Test
    @DisplayName("Should reject blank path")
    void shouldRejectPath_whenBlank() {
        assertThrows(FileValidationException.class, () -> fileStorageService.deleteFile("   "));
    }

    @Test
    @DisplayName("Should reject null path")
    void shouldRejectPath_whenNull() {
        assertThrows(FileValidationException.class, () -> fileStorageService.deleteFile(null));
    }

    @Test
    @DisplayName("Should generate presigned URL with custom expiration")
    void shouldGeneratePresignedUrl_withCustomExpiration() {
        // Given
        String path = "profiles/photo.jpg";
        String expectedUrl = "https://storage.example.com/presigned/photo.jpg";
        when(storageAdapter.generatePresignedUrl(path, 120)).thenReturn(expectedUrl);

        // When
        String url = fileStorageService.getFileUrl(path, 120);

        // Then
        assertEquals(expectedUrl, url);
        verify(storageAdapter).generatePresignedUrl(path, 120);
    }

    @Test
    void shouldStoreUserAvatarWithOrganizedPath() {
        MockMultipartFile file =
                new MockMultipartFile("file", "avatar.png", "image/png", "img".getBytes());
        String expectedPath = "proje-pazari-avatars/users/user-1/avatar.png";

        when(storageAdapter.store(any(), eq(expectedPath))).thenReturn(expectedPath);

        String storedPath = fileStorageService.storeUserAvatar("user-1", file);

        assertEquals(expectedPath, storedPath);
        verify(storageAdapter).store(any(), eq(expectedPath));
    }

    @Test
    void shouldStoreProjectDocumentWithOrganizedPath() {
        MockMultipartFile file =
                new MockMultipartFile("file", "spec.pdf", "application/pdf", "pdf".getBytes());
        String expectedPath = "proje-pazari-documents/projects/proj-1/doc-1.pdf";

        when(storageAdapter.store(any(), eq(expectedPath))).thenReturn(expectedPath);

        String storedPath = fileStorageService.storeProjectDocument("proj-1", "doc-1", file);

        assertEquals(expectedPath, storedPath);
        verify(storageAdapter).store(any(), eq(expectedPath));
    }

    @Test
    void shouldRejectInvalidUserIdForAvatarPath() {
        MockMultipartFile file =
                new MockMultipartFile("file", "avatar.png", "image/png", "img".getBytes());

        assertThrows(
                FileValidationException.class,
                () -> fileStorageService.storeUserAvatar("../bad", file));
    }
}
