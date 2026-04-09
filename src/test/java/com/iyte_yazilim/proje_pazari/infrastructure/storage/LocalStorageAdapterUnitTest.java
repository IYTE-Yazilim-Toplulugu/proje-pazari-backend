package com.iyte_yazilim.proje_pazari.infrastructure.storage;

import static org.junit.jupiter.api.Assertions.*;

import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FileValidationException;
import com.iyte_yazilim.proje_pazari.domain.models.FileMetadata;
import com.iyte_yazilim.proje_pazari.domain.models.FileUpload;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalStorageAdapterUnitTest {

    @TempDir Path tempDir;

    private LocalStorageAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new LocalStorageAdapter(tempDir.toString());
    }

    @Nested
    @DisplayName("store() method")
    class StoreTests {

        @Test
        @DisplayName("should store file and return API path")
        void shouldStoreFileAndReturnPath() {
            byte[] content = "test content".getBytes();
            FileUpload file = new FileUpload("test.txt", "text/plain", content, content.length);

            String result = adapter.store(file, "test/test.txt");

            assertEquals("/api/v1/files/test/test.txt", result);
            assertTrue(Files.exists(tempDir.resolve("test/test.txt")));
        }

        @Test
        @DisplayName("should overwrite existing file")
        void shouldOverwriteExistingFile() {
            byte[] original = "original".getBytes();
            byte[] updated = "updated content".getBytes();

            adapter.store(
                    new FileUpload("f.txt", "text/plain", original, original.length), "f.txt");
            adapter.store(new FileUpload("f.txt", "text/plain", updated, updated.length), "f.txt");

            assertTrue(adapter.exists("f.txt"));
        }

        @Test
        @DisplayName("should throw when path traversal detected")
        void shouldThrowOnPathTraversal() {
            byte[] content = "content".getBytes();
            FileUpload file = new FileUpload("test.txt", "text/plain", content, content.length);

            assertThrows(FileStorageException.class, () -> adapter.store(file, "../outside.txt"));
        }

        @Test
        @DisplayName("should create parent directories automatically")
        void shouldCreateParentDirectories() {
            byte[] content = "nested".getBytes();
            FileUpload file = new FileUpload("deep.txt", "text/plain", content, content.length);

            adapter.store(file, "a/b/c/deep.txt");

            assertTrue(Files.exists(tempDir.resolve("a/b/c/deep.txt")));
        }
    }

    @Nested
    @DisplayName("exists() method")
    class ExistsTests {

        @Test
        @DisplayName("should return true when file exists")
        void shouldReturnTrueWhenFileExists() {
            byte[] content = "content".getBytes();
            adapter.store(new FileUpload("f.txt", "text/plain", content, content.length), "f.txt");

            assertTrue(adapter.exists("f.txt"));
        }

        @Test
        @DisplayName("should return false when file does not exist")
        void shouldReturnFalseWhenFileNotExists() {
            assertFalse(adapter.exists("nonexistent.txt"));
        }

        @Test
        @DisplayName("should return false when path traversal attempted")
        void shouldReturnFalseOnPathTraversal() {
            assertFalse(adapter.exists("../outside.txt"));
        }
    }

    @Nested
    @DisplayName("delete() method")
    class DeleteTests {

        @Test
        @DisplayName("should delete existing file")
        void shouldDeleteExistingFile() {
            byte[] content = "content".getBytes();
            adapter.store(new FileUpload("f.txt", "text/plain", content, content.length), "f.txt");
            assertTrue(adapter.exists("f.txt"));

            adapter.delete("f.txt");

            assertFalse(adapter.exists("f.txt"));
        }

        @Test
        @DisplayName("should not throw when deleting non-existent file")
        void shouldNotThrowWhenFileNotExists() {
            assertDoesNotThrow(() -> adapter.delete("nonexistent.txt"));
        }

        @Test
        @DisplayName("should throw when path traversal detected")
        void shouldThrowOnPathTraversal() {
            assertThrows(FileValidationException.class, () -> adapter.delete("../outside.txt"));
        }
    }

    @Nested
    @DisplayName("getMetadata() method")
    class GetMetadataTests {

        @Test
        @DisplayName("should return metadata for existing file")
        void shouldReturnMetadataForExistingFile() {
            byte[] content = "metadata content".getBytes();
            adapter.store(
                    new FileUpload("meta.txt", "text/plain", content, content.length), "meta.txt");

            FileMetadata metadata = adapter.getMetadata("meta.txt");

            assertNotNull(metadata);
            assertEquals(content.length, metadata.getSize());
            assertNotNull(metadata.getLastModified());
        }

        @Test
        @DisplayName("should throw when file not found")
        void shouldThrowWhenFileNotFound() {
            assertThrows(
                    FileValidationException.class, () -> adapter.getMetadata("nonexistent.txt"));
        }

        @Test
        @DisplayName("should throw when path traversal detected")
        void shouldThrowOnPathTraversal() {
            assertThrows(
                    FileValidationException.class, () -> adapter.getMetadata("../outside.txt"));
        }
    }

    @Nested
    @DisplayName("retrieveAsBytes() method")
    class RetrieveAsBytesTests {

        @Test
        @DisplayName("should retrieve file content as bytes")
        void shouldRetrieveFileContent() {
            byte[] content = "hello bytes".getBytes();
            adapter.store(new FileUpload("f.txt", "text/plain", content, content.length), "f.txt");

            byte[] result = adapter.retrieveAsBytes("f.txt");

            assertArrayEquals(content, result);
        }

        @Test
        @DisplayName("should throw when file not found")
        void shouldThrowWhenFileNotFound() {
            assertThrows(
                    FileValidationException.class,
                    () -> adapter.retrieveAsBytes("nonexistent.txt"));
        }

        @Test
        @DisplayName("should throw when path traversal detected")
        void shouldThrowOnPathTraversal() {
            assertThrows(
                    FileValidationException.class, () -> adapter.retrieveAsBytes("../outside.txt"));
        }
    }

    @Nested
    @DisplayName("isAvailable() method")
    class IsAvailableTests {

        @Test
        @DisplayName("should return true when storage directory exists")
        void shouldReturnTrueWhenDirectoryExists() {
            assertTrue(adapter.isAvailable());
        }
    }

    @Nested
    @DisplayName("generatePresignedUrl() method")
    class GeneratePresignedUrlTests {

        @Test
        @DisplayName("should return API path as presigned URL")
        void shouldReturnApiPath() {
            String url = adapter.generatePresignedUrl("test/file.txt", 60);
            assertEquals("/api/v1/files/test/file.txt", url);
        }
    }

    @Nested
    @DisplayName("getTotalSpaceBytes() and getUsedSpaceBytes()")
    class SpaceTests {

        @Test
        @DisplayName("should return non-null total space")
        void shouldReturnTotalSpace() {
            assertNotNull(adapter.getTotalSpaceBytes());
            assertTrue(adapter.getTotalSpaceBytes() > 0);
        }

        @Test
        @DisplayName("should return non-null used space")
        void shouldReturnUsedSpace() {
            assertNotNull(adapter.getUsedSpaceBytes());
        }
    }

    @Nested
    @DisplayName("listBuckets() method")
    class ListBucketsTests {

        @Test
        @DisplayName("should return storage directory name as bucket")
        void shouldReturnStorageDirectoryAsBucket() {
            var buckets = adapter.listBuckets();
            assertFalse(buckets.isEmpty());
        }
    }
}
