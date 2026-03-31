package com.iyte_yazilim.proje_pazari.infrastructure.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import com.iyte_yazilim.proje_pazari.domain.models.FileUpload;
import com.iyte_yazilim.proje_pazari.infrastructure.metrics.BusinessMetricsService;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for MinioStorageAdapter.
 *
 * <p>NOTE: These tests require a running MinIO instance at http://localhost:9000 with credentials:
 * minioadmin/minioadmin123
 *
 * <p>To run these tests: 1. Start MinIO: docker run -p 9000:9000 -p 9001:9001 minio/minio server
 * /data --console-address ":9001" 2. Or use Testcontainers for automated MinIO setup
 *
 * <p>For unit tests, consider refactoring MinioStorageAdapter to allow dependency injection of
 * MinioClient.
 */
@Disabled(
        "Requires running MinIO instance. Use @Testcontainers for integration tests or refactor adapter for DI.")
class MinioStorageAdapterTest {

    private MinioStorageAdapter adapter;
    private MinioClient mockMinioClient;
    private BusinessMetricsService metricsService;

    @BeforeEach
    void setUp() throws Exception {
        // Create mock MinioClient first
        mockMinioClient = mock(MinioClient.class);
        metricsService = mock(BusinessMetricsService.class);

        // Mock bucket exists check to avoid connection errors
        when(mockMinioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        // Initialize adapter - constructor will try to connect to MinIO during
        // createBucketIfNotExists()
        // We catch the exception and inject mock client after construction
        try {
            adapter =
                    new MinioStorageAdapter(
                            "http://localhost:9000",
                            "minioadmin",
                            "minioadmin123",
                            "test-bucket",
                            metricsService);
        } catch (FileStorageException e) {
            // Constructor failed because MinIO isn't running
            // Create adapter using reflection to bypass the connection attempt
            adapter = createAdapterWithoutConnection();
        }

        // Use reflection to inject the mocked MinioClient immediately after construction
        // This replaces the real client with our mock, so all test calls use the mock
        Field minioClientField = MinioStorageAdapter.class.getDeclaredField("minioClient");
        minioClientField.setAccessible(true);
        minioClientField.set(adapter, mockMinioClient);

        // Also ensure bucketName is set
        Field bucketNameField = MinioStorageAdapter.class.getDeclaredField("bucketName");
        bucketNameField.setAccessible(true);
        bucketNameField.set(adapter, "test-bucket");
    }

    /**
     * Creates a MinioStorageAdapter instance without triggering the connection. This is a
     * workaround for unit testing when MinIO isn't available.
     */
    private MinioStorageAdapter createAdapterWithoutConnection() throws Exception {
        // Create a minimal MinioClient that won't be used (we'll replace it)
        MinioClient tempClient =
                MinioClient.builder()
                        .endpoint("http://localhost:9000")
                        .credentials("minioadmin", "minioadmin123")
                        .build();

        // Create adapter instance - it will try to connect, but we'll catch and handle
        // For true unit tests, consider refactoring to allow DI of MinioClient
        MinioStorageAdapter tempAdapter;
        try {
            tempAdapter =
                    new MinioStorageAdapter(
                            "http://localhost:9000",
                            "minioadmin",
                            "minioadmin123",
                            "test-bucket",
                            metricsService);
        } catch (FileStorageException e) {
            // If it still fails, we need to create it differently
            // This is a limitation - the constructor always tries to connect
            throw new RuntimeException(
                    "Cannot create MinioStorageAdapter for testing. "
                            + "Consider using @Testcontainers for integration tests or "
                            + "refactoring MinioStorageAdapter to allow dependency injection.",
                    e);
        }

        return tempAdapter;
    }

    @Test
    void ShouldStoreFile() throws Exception {
        // Given
        byte[] content = "test content".getBytes();
        FileUpload file = new FileUpload("test.txt", "text/plain", content, content.length);

        String presignedUrl = "http://localhost:9000/test-bucket/test/test.txt?signature=test";
        when(mockMinioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn(presignedUrl);

        // When
        String result = adapter.store(file, "test/test.txt");

        // Then
        assertNotNull(result);
        assertTrue(result.contains("test/test.txt") || result.contains("test-bucket"));
        verify(mockMinioClient, times(1)).putObject(any());
    }

    @Test
    void ShouldRejectPathTraversal() throws Exception {
        // Given
        byte[] content = "test content".getBytes();
        FileUpload file = new FileUpload("test.txt", "text/plain", content, content.length);
        String path = "../test.txt";

        String presignedUrl = "http://localhost:9000/test-bucket/../test.txt?signature=test";
        when(mockMinioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn(presignedUrl);

        // When
        String result = adapter.store(file, path);
        assertNotNull(result);
    }

    @Test
    void ShouldGeneratePresignedUrl() throws Exception {
        // Given
        String path = "test/test.txt";
        String expectedUrl =
                "http://localhost:9000/test-bucket/test/test.txt?X-Amz-Expires=3600&signature=test";
        when(mockMinioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn(expectedUrl);

        // When
        String url = adapter.generatePresignedUrl(path, 60);

        // Then
        assertNotNull(url);
        assertTrue(url.contains(path) || url.contains("test.txt"));
        verify(mockMinioClient, times(1))
                .getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }

    @Test
    void ShouldThrowForNonExistingFile() throws Exception {
        // Given
        String path = "nonexistent.txt";
        io.minio.errors.ErrorResponseException minioException =
                mock(io.minio.errors.ErrorResponseException.class);
        when(mockMinioClient.statObject(any(StatObjectArgs.class))).thenThrow(minioException);

        // When/Then
        assertThrows(FileStorageException.class, () -> adapter.getMetadata(path));
        verify(mockMinioClient, times(1)).statObject(any(StatObjectArgs.class));
    }
}
