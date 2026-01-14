package com.iyte_yazilim.proje_pazari.infrastructure.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import com.iyte_yazilim.proje_pazari.domain.models.FileMetadata;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import java.io.ByteArrayInputStream;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

/**
 * Unit tests for MinioStorageAdapter using mocks. These tests verify the adapter's behavior without
 * requiring a running MinIO instance.
 */
@ExtendWith(MockitoExtension.class)
class MinioStorageAdapterUnitTest {

    @Mock private MinioClient mockMinioClient;
    @Mock private MultipartFile mockFile;
    private MinioStorageAdapter adapter;

    @BeforeEach
    void setUp() throws Exception {
        // Create adapter using the package-private constructor for testing with mock client
        adapter = new MinioStorageAdapter(mockMinioClient, "test-bucket");
    }

    @Nested
    @DisplayName("store() method")
    class StoreTests {

        @Test
        @DisplayName("should store file and return path")
        void shouldStoreFileAndReturnPath() throws Exception {
            // Given
            byte[] content = "test content".getBytes();
            when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(content));
            when(mockFile.getContentType()).thenReturn("text/plain");
            when(mockFile.getSize()).thenReturn((long) content.length);
            String path = "test/test.txt";

            // When
            String result = adapter.store(mockFile, path);

            // Then
            assertEquals(path, result);
            verify(mockMinioClient, times(1)).putObject(any(PutObjectArgs.class));
        }

        @Test
        @DisplayName("should throw FileStorageException when upload fails")
        void shouldThrowExceptionWhenUploadFails() throws Exception {
            // Given
            byte[] content = "test content".getBytes();
            when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(content));
            when(mockFile.getContentType()).thenReturn("text/plain");
            when(mockFile.getSize()).thenReturn((long) content.length);
            String path = "test/test.txt";

            doThrow(new RuntimeException("Upload failed"))
                    .when(mockMinioClient)
                    .putObject(any(PutObjectArgs.class));

            // When/Then
            FileStorageException exception =
                    assertThrows(FileStorageException.class, () -> adapter.store(mockFile, path));
            assertTrue(exception.getMessage().contains("Failed to upload file"));
        }

        @Test
        @DisplayName("should handle empty file")
        void shouldHandleEmptyFile() throws Exception {
            // Given
            byte[] content = new byte[0];
            when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(content));
            when(mockFile.getContentType()).thenReturn("text/plain");
            when(mockFile.getSize()).thenReturn(0L);
            String path = "test/empty.txt";

            // When
            String result = adapter.store(mockFile, path);

            // Then
            assertEquals(path, result);
            verify(mockMinioClient, times(1)).putObject(any(PutObjectArgs.class));
        }

        @Test
        @DisplayName("should handle file with different content types")
        void shouldHandleDifferentContentTypes() throws Exception {
            // Given
            byte[] content = new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47}; // PNG header
            when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(content));
            when(mockFile.getContentType()).thenReturn("image/png");
            when(mockFile.getSize()).thenReturn((long) content.length);
            String path = "images/test.png";

            // When
            String result = adapter.store(mockFile, path);

            // Then
            assertEquals(path, result);
            verify(mockMinioClient, times(1)).putObject(any(PutObjectArgs.class));
        }
    }

    @Nested
    @DisplayName("generatePresignedUrl() method")
    class GeneratePresignedUrlTests {

        @Test
        @DisplayName("should generate valid presigned URL")
        void shouldGenerateValidPresignedUrl() throws Exception {
            // Given
            String path = "test/test.txt";
            String expectedUrl =
                    "http://localhost:9000/test-bucket/test/test.txt?X-Amz-Algorithm=...";
            when(mockMinioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                    .thenReturn(expectedUrl);

            // When
            String url = adapter.generatePresignedUrl(path, 60);

            // Then
            assertEquals(expectedUrl, url);
            verify(mockMinioClient, times(1))
                    .getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
        }

        @Test
        @DisplayName("should throw FileStorageException when URL generation fails")
        void shouldThrowExceptionWhenUrlGenerationFails() throws Exception {
            // Given
            String path = "test/test.txt";
            when(mockMinioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                    .thenThrow(new RuntimeException("URL generation failed"));

            // When/Then
            FileStorageException exception =
                    assertThrows(
                            FileStorageException.class,
                            () -> adapter.generatePresignedUrl(path, 60));
            assertTrue(exception.getMessage().contains("Failed to generate presigned URL"));
        }
    }

    @Nested
    @DisplayName("delete() method")
    class DeleteTests {

        @Test
        @DisplayName("should delete file successfully")
        void shouldDeleteFile() throws Exception {
            // Given
            String path = "test/test.txt";

            // When
            adapter.delete(path);

            // Then
            verify(mockMinioClient, times(1)).removeObject(any(RemoveObjectArgs.class));
        }

        @Test
        @DisplayName("should throw FileStorageException when delete fails")
        void shouldThrowExceptionWhenDeleteFails() throws Exception {
            // Given
            String path = "test/test.txt";
            doThrow(new RuntimeException("Delete failed"))
                    .when(mockMinioClient)
                    .removeObject(any(RemoveObjectArgs.class));

            // When/Then
            FileStorageException exception =
                    assertThrows(FileStorageException.class, () -> adapter.delete(path));
            assertTrue(exception.getMessage().contains("Failed to delete file"));
        }
    }

    @Nested
    @DisplayName("exists() method")
    class ExistsTests {

        @Test
        @DisplayName("should return true when file exists")
        void shouldReturnTrueWhenFileExists() throws Exception {
            // Given
            String path = "test/test.txt";
            StatObjectResponse mockResponse = mock(StatObjectResponse.class);
            when(mockMinioClient.statObject(any(StatObjectArgs.class))).thenReturn(mockResponse);

            // When
            boolean exists = adapter.exists(path);

            // Then
            assertTrue(exists);
        }

        @Test
        @DisplayName("should return false when file does not exist")
        void shouldReturnFalseWhenFileDoesNotExist() throws Exception {
            // Given
            String path = "nonexistent/file.txt";
            // MinIO throws an exception when statObject is called for a non-existent file
            when(mockMinioClient.statObject(any(StatObjectArgs.class)))
                    .thenThrow(new RuntimeException("Object does not exist"));

            // When
            boolean exists = adapter.exists(path);

            // Then
            assertFalse(exists);
        }
    }

    @Nested
    @DisplayName("getMetadata() method")
    class GetMetadataTests {

        @Test
        @DisplayName("should return file metadata")
        void shouldReturnFileMetadata() throws Exception {
            // Given
            String path = "test/metadata-test.txt";
            ZonedDateTime lastModified = ZonedDateTime.now();

            StatObjectResponse mockResponse = mock(StatObjectResponse.class);
            when(mockResponse.size()).thenReturn(100L);
            when(mockResponse.contentType()).thenReturn("text/plain");
            when(mockResponse.lastModified()).thenReturn(lastModified);
            when(mockResponse.etag()).thenReturn("abc123");

            when(mockMinioClient.statObject(any(StatObjectArgs.class))).thenReturn(mockResponse);

            // When
            FileMetadata metadata = adapter.getMetadata(path);

            // Then
            assertNotNull(metadata);
            assertEquals(path, metadata.getPath());
            assertEquals(100L, metadata.getSize());
            assertEquals("text/plain", metadata.getContentType());
            assertEquals("abc123", metadata.getEtag());
            assertEquals("metadata-test.txt", metadata.getFileName());
        }

        @Test
        @DisplayName("should throw FileStorageException when file not found")
        void shouldThrowExceptionWhenFileNotFound() throws Exception {
            // Given
            String path = "nonexistent/file.txt";
            when(mockMinioClient.statObject(any(StatObjectArgs.class)))
                    .thenThrow(new RuntimeException("Object does not exist"));

            // When/Then
            FileStorageException exception =
                    assertThrows(FileStorageException.class, () -> adapter.getMetadata(path));
            assertTrue(exception.getMessage().contains("Failed to get file metadata"));
        }

        @Test
        @DisplayName("should extract filename from nested path")
        void shouldExtractFilenameFromNestedPath() throws Exception {
            // Given
            String path = "level1/level2/level3/deep-file.txt";
            ZonedDateTime lastModified = ZonedDateTime.now();

            StatObjectResponse mockResponse = mock(StatObjectResponse.class);
            when(mockResponse.size()).thenReturn(50L);
            when(mockResponse.contentType()).thenReturn("text/plain");
            when(mockResponse.lastModified()).thenReturn(lastModified);
            when(mockResponse.etag()).thenReturn("xyz789");

            when(mockMinioClient.statObject(any(StatObjectArgs.class))).thenReturn(mockResponse);

            // When
            FileMetadata metadata = adapter.getMetadata(path);

            // Then
            assertEquals("deep-file.txt", metadata.getFileName());
        }
    }
}
