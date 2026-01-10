package com.iyte_yazilim.proje_pazari.infrastructure.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import com.iyte_yazilim.proje_pazari.domain.models.FileMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration tests for MinioStorageAdapter using Testcontainers. These tests run against a real
 * MinIO instance in a Docker container.
 *
 * <p>These tests require Docker to be available. If Docker is not accessible, tests will be
 * skipped automatically.
 *
 * <p>Note: On some systems (e.g., Docker Desktop on Linux), you may need to configure
 * Testcontainers. See: https://java.testcontainers.org/supported_docker_environment/
 */
@Testcontainers(disabledWithoutDocker = true)
class MinioStorageAdapterIntegrationTest {

    private static final String ACCESS_KEY = "minioadmin";
    private static final String SECRET_KEY = "minioadmin123";
    private static final String BUCKET_NAME = "test-bucket";

    @Container
    static MinIOContainer minioContainer =
            new MinIOContainer("minio/minio:latest")
                    .withUserName(ACCESS_KEY)
                    .withPassword(SECRET_KEY);

    private MinioStorageAdapter adapter;

    @BeforeEach
    void setUp() {
        assumeTrue(
                minioContainer != null && minioContainer.isRunning(),
                "MinIO container should be running");
        String minioUrl = minioContainer.getS3URL();
        adapter = new MinioStorageAdapter(minioUrl, ACCESS_KEY, SECRET_KEY, BUCKET_NAME);
    }

    @Test
    void shouldStoreAndRetrieveFile() {
        // Given
        byte[] content = "Hello, MinIO!".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", content);
        String path = "test/hello.txt";

        // When
        String url = adapter.store(file, path);

        // Then
        assertNotNull(url);
        assertTrue(url.contains(path) || url.contains("hello.txt"));
        assertTrue(adapter.exists(path));
    }

    @Test
    void shouldGeneratePresignedUrl() {
        // Given
        byte[] content = "Test content".getBytes();
        MockMultipartFile file =
                new MockMultipartFile("file", "presigned-test.txt", "text/plain", content);
        String path = "test/presigned-test.txt";
        adapter.store(file, path);

        // When
        String presignedUrl = adapter.generatePresignedUrl(path, 60);

        // Then
        assertNotNull(presignedUrl);
        assertTrue(presignedUrl.contains("X-Amz") || presignedUrl.contains(path));
    }

    @Test
    void shouldDeleteFile() {
        // Given
        byte[] content = "To be deleted".getBytes();
        MockMultipartFile file =
                new MockMultipartFile("file", "delete-test.txt", "text/plain", content);
        String path = "test/delete-test.txt";
        adapter.store(file, path);
        assertTrue(adapter.exists(path));

        // When
        adapter.delete(path);

        // Then
        assertFalse(adapter.exists(path));
    }

    @Test
    void shouldReturnFalseForNonExistentFile() {
        // When
        boolean exists = adapter.exists("nonexistent/file.txt");

        // Then
        assertFalse(exists);
    }

    @Test
    void shouldGetFileMetadata() {
        // Given
        byte[] content = "Metadata test content".getBytes();
        MockMultipartFile file =
                new MockMultipartFile("file", "metadata-test.txt", "text/plain", content);
        String path = "test/metadata-test.txt";
        adapter.store(file, path);

        // When
        FileMetadata metadata = adapter.getMetadata(path);

        // Then
        assertNotNull(metadata);
        assertEquals(path, metadata.getPath());
        assertEquals(content.length, metadata.getSize());
        assertEquals("text/plain", metadata.getContentType());
        assertNotNull(metadata.getLastModified());
    }

    @Test
    void shouldThrowExceptionForNonExistentFileMetadata() {
        // When/Then
        assertThrows(
                FileStorageException.class,
                () -> {
                    adapter.getMetadata("nonexistent/file.txt");
                });
    }

    @Test
    void shouldStoreFileWithDifferentContentTypes() {
        // Given - Image file
        byte[] imageContent = new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47}; // PNG header
        MockMultipartFile imageFile =
                new MockMultipartFile("file", "image.png", "image/png", imageContent);
        String imagePath = "profiles/user123.png";

        // When
        String imageUrl = adapter.store(imageFile, imagePath);

        // Then
        assertNotNull(imageUrl);
        assertTrue(adapter.exists(imagePath));

        FileMetadata metadata = adapter.getMetadata(imagePath);
        assertEquals("image/png", metadata.getContentType());
    }

    @Test
    void shouldHandleNestedPaths() {
        // Given
        byte[] content = "Nested content".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "deep.txt", "text/plain", content);
        String path = "level1/level2/level3/deep.txt";

        // When
        String url = adapter.store(file, path);

        // Then
        assertNotNull(url);
        assertTrue(adapter.exists(path));
    }
}
