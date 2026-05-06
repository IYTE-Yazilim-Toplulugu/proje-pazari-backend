package com.iyte_yazilim.proje_pazari.infrastructure.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FileValidationException;
import com.iyte_yazilim.proje_pazari.domain.models.FileMetadata;
import com.iyte_yazilim.proje_pazari.domain.models.FileUpload;
import com.iyte_yazilim.proje_pazari.infrastructure.metrics.BusinessMetricsService;
import io.micrometer.core.instrument.Timer;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import java.time.ZonedDateTime;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for MinioStorageAdapter using mocks. These tests verify the adapter's behavior without
 * requiring a running MinIO instance.
 */
@ExtendWith(MockitoExtension.class)
class MinioStorageAdapterUnitTest {

    @Mock private MinioClient mockMinioClient;
    @Mock private BusinessMetricsService metricsService;
    @Mock private Timer uploadTimer;

    private MinioStorageAdapter adapter;

    @BeforeEach
    void setUp() {
        lenient().when(metricsService.getMinioUploadTimer()).thenReturn(uploadTimer);
        lenient()
                .when(uploadTimer.record(any(Supplier.class)))
                .thenAnswer(inv -> ((Supplier<?>) inv.getArgument(0)).get());
        // Create adapter using the package-private constructor for testing with mock client
        adapter = new MinioStorageAdapter(mockMinioClient, "test-bucket", metricsService);
    }

    @Nested
    @DisplayName("store() method")
    class StoreTests {

        @Test
        @DisplayName("should store file and return path")
        void shouldStoreFileAndReturnPath() throws Exception {
            byte[] content = "test content".getBytes();
            FileUpload file = new FileUpload("test.txt", "text/plain", content, content.length);

            String result = adapter.store(file, "test/test.txt");

            assertEquals("test/test.txt", result);
            verify(mockMinioClient, times(1)).putObject(any(PutObjectArgs.class));
        }

        @Test
        @DisplayName("should throw FileStorageException when upload fails")
        void shouldThrowExceptionWhenUploadFails() throws Exception {
            byte[] content = "test content".getBytes();
            FileUpload file = new FileUpload("test.txt", "text/plain", content, content.length);

            doThrow(new RuntimeException("Upload failed"))
                    .when(mockMinioClient)
                    .putObject(any(PutObjectArgs.class));

            FileStorageException exception =
                    assertThrows(
                            FileStorageException.class, () -> adapter.store(file, "test/test.txt"));
            assertTrue(exception.getMessage().contains("Failed to upload file"));
        }

        @Test
        @DisplayName("should handle empty file")
        void shouldHandleEmptyFile() throws Exception {
            byte[] content = new byte[0];
            FileUpload file = new FileUpload("empty.txt", "text/plain", content, 0);

            String result = adapter.store(file, "test/empty.txt");

            assertEquals("test/empty.txt", result);
            verify(mockMinioClient, times(1)).putObject(any(PutObjectArgs.class));
        }

        @Test
        @DisplayName("should handle file with different content types")
        void shouldHandleDifferentContentTypes() throws Exception {
            byte[] content = new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47};
            FileUpload file = new FileUpload("test.png", "image/png", content, content.length);

            String result = adapter.store(file, "images/test.png");

            assertEquals("images/test.png", result);
            verify(mockMinioClient, times(1)).putObject(any(PutObjectArgs.class));
        }

        @Test
        @DisplayName("should store file when explicit bucket prefix is provided")
        void shouldStoreFileWithBucketPrefixedPath() throws Exception {
            byte[] content = "avatar".getBytes();
            FileUpload file = new FileUpload("avatar.png", "image/png", content, content.length);
            String path = "proje-pazari-avatars/users/u1/avatar.png";

            String result = adapter.store(file, path);

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
            assertEquals(path, metadata.path());
            assertEquals(100L, metadata.size());
            assertEquals("text/plain", metadata.contentType());
            assertEquals("abc123", metadata.etag());
            assertEquals("metadata-test.txt", metadata.fileName());
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
            assertEquals("deep-file.txt", metadata.fileName());
        }
    }

    @Nested
    @DisplayName("isAvailable() method")
    class IsAvailableTests {

        @Test
        @DisplayName("should return true when storage is available")
        void shouldReturnTrueWhenAvailable() throws Exception {
            when(mockMinioClient.bucketExists(any(io.minio.BucketExistsArgs.class)))
                    .thenReturn(true);
            when(mockMinioClient.listBuckets()).thenReturn(java.util.List.of());

            assertTrue(adapter.isAvailable());
        }

        @Test
        @DisplayName("should return false when bucket does not exist")
        void shouldReturnFalseWhenBucketNotExist() throws Exception {
            when(mockMinioClient.bucketExists(any(io.minio.BucketExistsArgs.class)))
                    .thenReturn(false);

            assertFalse(adapter.isAvailable());
        }

        @Test
        @DisplayName("should return false when exception thrown")
        void shouldReturnFalseWhenExceptionThrown() throws Exception {
            when(mockMinioClient.bucketExists(any(io.minio.BucketExistsArgs.class)))
                    .thenThrow(new RuntimeException("Connection failed"));

            assertFalse(adapter.isAvailable());
        }
    }

    @Nested
    @DisplayName("listBuckets() method")
    class ListBucketsTests {

        @Test
        @DisplayName("should return bucket names")
        void shouldReturnBucketNames() throws Exception {
            io.minio.messages.Bucket mockBucket = mock(io.minio.messages.Bucket.class);
            when(mockBucket.name()).thenReturn("test-bucket");
            when(mockMinioClient.listBuckets()).thenReturn(java.util.List.of(mockBucket));

            java.util.List<String> buckets = adapter.listBuckets();

            assertEquals(1, buckets.size());
            assertEquals("test-bucket", buckets.get(0));
        }

        @Test
        @DisplayName("should return empty list when exception thrown")
        void shouldReturnEmptyListWhenExceptionThrown() throws Exception {
            when(mockMinioClient.listBuckets())
                    .thenThrow(new RuntimeException("Connection failed"));

            java.util.List<String> buckets = adapter.listBuckets();

            assertTrue(buckets.isEmpty());
        }
    }

    @Nested
    @DisplayName("path validation")
    class PathValidationTests {

        @Test
        @DisplayName("should throw FileStorageException when path is null")
        void shouldThrowWhenPathIsNull() {
            byte[] content = "content".getBytes();
            FileUpload file = new FileUpload("test.txt", "text/plain", content, content.length);

            assertThrows(FileValidationException.class, () -> adapter.store(file, null));
        }

        @Test
        @DisplayName("should throw FileStorageException when path is blank")
        void shouldThrowWhenPathIsBlank() {
            byte[] content = "content".getBytes();
            FileUpload file = new FileUpload("test.txt", "text/plain", content, content.length);

            assertThrows(FileValidationException.class, () -> adapter.store(file, "   "));
        }

        @Test
        @DisplayName("should strip leading slashes from path")
        void shouldStripLeadingSlashes() throws Exception {
            byte[] content = "content".getBytes();
            FileUpload file = new FileUpload("test.txt", "text/plain", content, content.length);

            String result = adapter.store(file, "/test/test.txt");

            assertEquals("test/test.txt", result);
            verify(mockMinioClient, times(1)).putObject(any(io.minio.PutObjectArgs.class));
        }
    }

    @Nested
    @DisplayName("delete() with metrics")
    class DeleteWithMetricsTests {

        @Test
        @DisplayName("should increment delete success metric")
        void shouldIncrementDeleteSuccessMetric() throws Exception {
            adapter.delete("test/file.txt");

            verify(metricsService, times(1)).incrementMinioDeleteSuccess();
        }

        @Test
        @DisplayName("should increment delete failure metric when delete fails")
        void shouldIncrementDeleteFailureMetric() throws Exception {
            doThrow(new RuntimeException("Delete failed"))
                    .when(mockMinioClient)
                    .removeObject(any(io.minio.RemoveObjectArgs.class));

            assertThrows(FileStorageException.class, () -> adapter.delete("test/file.txt"));

            verify(metricsService, times(1)).incrementMinioDeleteFailure();
        }
    }

    @Nested
    @DisplayName("generatePresignedUrl() with metrics")
    class PresignedUrlWithMetricsTests {

        @Test
        @DisplayName("should increment download success metric")
        void shouldIncrementDownloadSuccessMetric() throws Exception {
            when(mockMinioClient.getPresignedObjectUrl(
                            any(io.minio.GetPresignedObjectUrlArgs.class)))
                    .thenReturn("http://example.com/file");

            adapter.generatePresignedUrl("test/file.txt", 60);

            verify(metricsService, times(1)).incrementMinioDownloadSuccess();
        }

        @Test
        @DisplayName("should increment download failure metric when URL generation fails")
        void shouldIncrementDownloadFailureMetric() throws Exception {
            when(mockMinioClient.getPresignedObjectUrl(
                            any(io.minio.GetPresignedObjectUrlArgs.class)))
                    .thenThrow(new RuntimeException("Failed"));

            assertThrows(
                    FileStorageException.class,
                    () -> adapter.generatePresignedUrl("test/file.txt", 60));

            verify(metricsService, times(1)).incrementMinioDownloadFailure();
        }
    }
}
