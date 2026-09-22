package com.iyte_yazilim.proje_pazari.infrastructure.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FileValidationException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.StoredFileNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.models.FileMetadata;
import com.iyte_yazilim.proje_pazari.domain.models.FileUpload;
import com.iyte_yazilim.proje_pazari.domain.models.StorageDownloadResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;
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
            assertEquals(content.length, metadata.size());
            assertNotNull(metadata.lastModified());
        }

        @Test
        @DisplayName("should throw not-found when file is missing")
        void shouldThrowWhenFileNotFound() {
            assertThrows(
                    StoredFileNotFoundException.class,
                    () -> adapter.getMetadata("nonexistent.txt"));
        }

        @Test
        @DisplayName("should throw when path traversal detected")
        void shouldThrowOnPathTraversal() {
            assertThrows(
                    FileValidationException.class, () -> adapter.getMetadata("../outside.txt"));
        }
    }

    @Nested
    @DisplayName("resolveDownload() method")
    class ResolveDownloadTests {

        @Test
        @DisplayName("should return inline content rather than a redirect URL")
        void shouldReturnInlineContent() {
            byte[] content = "hello bytes".getBytes();
            adapter.store(new FileUpload("f.txt", "text/plain", content, content.length), "f.txt");

            StorageDownloadResult result = adapter.resolveDownload("f.txt", 60);

            assertInstanceOf(StorageDownloadResult.InlineResult.class, result);
            StorageDownloadResult.InlineResult inline = (StorageDownloadResult.InlineResult) result;
            assertArrayEquals(content, inline.content());
            assertEquals("f.txt", inline.filename());
        }

        @Test
        @DisplayName("should return the content type recorded at store time")
        void shouldReturnRecordedContentType() {
            byte[] content = "not really a jpeg".getBytes();
            adapter.store(
                    new FileUpload("avatar.jpg", "image/jpeg", content, content.length),
                    "avatar.jpg");

            StorageDownloadResult.InlineResult inline =
                    (StorageDownloadResult.InlineResult) adapter.resolveDownload("avatar.jpg", 60);

            assertEquals("image/jpeg", inline.contentType());
        }

        @Test
        @DisplayName("recorded content type wins over the stored filename's extension")
        void shouldPreferRecordedContentTypeOverExtension() {
            byte[] content = "<script>alert(1)</script>".getBytes();
            adapter.store(
                    new FileUpload("payload.html", "image/jpeg", content, content.length),
                    "payload.html");

            StorageDownloadResult.InlineResult inline =
                    (StorageDownloadResult.InlineResult)
                            adapter.resolveDownload("payload.html", 60);

            assertEquals("image/jpeg", inline.contentType());
        }

        @Test
        @DisplayName(
                "should fall back to the extension, not the host mime database, without a record")
        void shouldResolveContentTypeFromExtensionWithoutRecord() throws Exception {
            // Written directly, so no sidecar exists — the case for fixtures and for files
            // stored before content types were recorded.
            Files.write(tempDir.resolve("seeded.jpg"), "bytes".getBytes());

            StorageDownloadResult.InlineResult inline =
                    (StorageDownloadResult.InlineResult) adapter.resolveDownload("seeded.jpg", 60);

            assertEquals("image/jpeg", inline.contentType());
        }

        @Test
        @DisplayName("should fall back to octet-stream for an unknown extension")
        void shouldFallBackForUnknownExtension() {
            byte[] content = "opaque".getBytes();
            adapter.store(
                    new FileUpload(
                            "blob.zzzzz", "application/octet-stream", content, content.length),
                    "blob.zzzzz");

            StorageDownloadResult.InlineResult inline =
                    (StorageDownloadResult.InlineResult) adapter.resolveDownload("blob.zzzzz", 60);

            assertEquals("application/octet-stream", inline.contentType());
        }

        @Test
        @DisplayName("should throw not-found (mapping to 404) when file is missing")
        void shouldThrowWhenFileNotFound() {
            assertThrows(
                    StoredFileNotFoundException.class,
                    () -> adapter.resolveDownload("nonexistent.txt", 60));
        }

        @Test
        @DisplayName("should throw validation error (mapping to 400) when path traversal detected")
        void shouldThrowOnPathTraversal() {
            assertThrows(
                    FileValidationException.class,
                    () -> adapter.resolveDownload("../outside.txt", 60));
        }

        @Test
        @DisplayName("should never return a URL pointing back at the download endpoint")
        void shouldNotReturnSelfReferentialRedirect() {
            byte[] content = "bytes".getBytes();
            adapter.store(new FileUpload("f.txt", "text/plain", content, content.length), "f.txt");

            StorageDownloadResult result = adapter.resolveDownload("f.txt", 60);

            assertFalse(result instanceof StorageDownloadResult.RedirectResult);
        }
    }

    @Nested
    @DisplayName("content type metadata sidecars")
    class MetadataSidecarTests {

        @Test
        @DisplayName("should not be readable through the adapter")
        void shouldNotBeReadable() {
            byte[] content = "bytes".getBytes();
            adapter.store(
                    new FileUpload("avatar.jpg", "image/jpeg", content, content.length),
                    "avatar.jpg");
            assertTrue(Files.exists(tempDir.resolve("avatar.jpg.meta")));

            assertThrows(
                    StoredFileNotFoundException.class,
                    () -> adapter.resolveDownload("avatar.jpg.meta", 60));
            assertThrows(
                    StoredFileNotFoundException.class,
                    () -> adapter.getMetadata("avatar.jpg.meta"));
            assertFalse(adapter.exists("avatar.jpg.meta"));
        }

        @Test
        @DisplayName("should not be writable through the adapter")
        void shouldNotBeWritable() {
            byte[] content = "contentType=text/html".getBytes();
            FileUpload file = new FileUpload("x.meta", "image/jpeg", content, content.length);

            assertThrows(FileStorageException.class, () -> adapter.store(file, "avatar.jpg.meta"));
        }

        @Test
        @DisplayName("should be removed together with the file they describe")
        void shouldBeDeletedWithFile() {
            byte[] content = "bytes".getBytes();
            adapter.store(
                    new FileUpload("avatar.jpg", "image/jpeg", content, content.length),
                    "avatar.jpg");

            adapter.delete("avatar.jpg");

            assertFalse(Files.exists(tempDir.resolve("avatar.jpg.meta")));
        }

        @Test
        @DisplayName("should not leak a previous content type to a replacement upload")
        void shouldNotLeakContentTypeToReplacement() {
            byte[] content = "bytes".getBytes();
            adapter.store(
                    new FileUpload("avatar.jpg", "image/jpeg", content, content.length),
                    "avatar.jpg");

            adapter.store(
                    new FileUpload("avatar.jpg", null, content, content.length), "avatar.jpg");

            assertFalse(Files.exists(tempDir.resolve("avatar.jpg.meta")));
        }
    }

    @Nested
    @DisplayName("symbolic link containment")
    class SymlinkContainmentTests {

        private Path outsideFile;

        @BeforeEach
        void createOutsideTarget(@TempDir Path outsideDir) throws Exception {
            outsideFile = outsideDir.resolve("secret.txt");
            Files.write(outsideFile, "host secret".getBytes());
        }

        private void linkOrSkip(Path link, Path target) {
            try {
                Files.createSymbolicLink(link, target);
            } catch (IOException | UnsupportedOperationException e) {
                assumeTrue(false, "symbolic links unsupported here: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("should reject reading through a link that points outside the root")
        void shouldRejectLinkedFile() {
            linkOrSkip(tempDir.resolve("public-link"), outsideFile);

            assertThrows(
                    FileValidationException.class,
                    () -> adapter.resolveDownload("public-link", 60));
            assertThrows(FileValidationException.class, () -> adapter.getMetadata("public-link"));
            assertFalse(adapter.exists("public-link"));
        }

        @Test
        @DisplayName("should reject reading through a linked directory")
        void shouldRejectLinkedDirectory() {
            linkOrSkip(tempDir.resolve("linked-dir"), outsideFile.getParent());

            assertThrows(
                    FileValidationException.class,
                    () -> adapter.resolveDownload("linked-dir/secret.txt", 60));
            assertFalse(adapter.exists("linked-dir/secret.txt"));
        }

        @Test
        @DisplayName("should reject writing through a linked directory")
        void shouldRejectStoreThroughLinkedDirectory() {
            linkOrSkip(tempDir.resolve("linked-dir"), outsideFile.getParent());
            byte[] content = "planted".getBytes();
            FileUpload file = new FileUpload("x.jpg", "image/jpeg", content, content.length);

            assertThrows(FileStorageException.class, () -> adapter.store(file, "linked-dir/x.jpg"));
            assertFalse(Files.exists(outsideFile.getParent().resolve("x.jpg")));
        }

        @Test
        @DisplayName("should reject deleting through a link that points outside the root")
        void shouldRejectDeleteThroughLink() {
            linkOrSkip(tempDir.resolve("public-link"), outsideFile);

            assertThrows(FileValidationException.class, () -> adapter.delete("public-link"));
            assertTrue(Files.exists(outsideFile));
        }

        @Test
        @DisplayName("should not write a content type sidecar through a planted link")
        void shouldRejectSidecarWriteThroughLink() throws Exception {
            linkOrSkip(tempDir.resolve("avatar.jpg.meta"), outsideFile);
            byte[] content = "bytes".getBytes();

            adapter.store(
                    new FileUpload("avatar.jpg", "image/jpeg", content, content.length),
                    "avatar.jpg");

            assertArrayEquals("host secret".getBytes(), Files.readAllBytes(outsideFile));
            assertArrayEquals(content, Files.readAllBytes(tempDir.resolve("avatar.jpg")));
        }

        @Test
        @DisplayName("should not read a content type sidecar through a planted link")
        void shouldRejectSidecarReadThroughLink() throws Exception {
            Files.write(outsideFile, "contentType=text/html".getBytes());
            byte[] content = "bytes".getBytes();
            Files.write(tempDir.resolve("avatar.jpg"), content);
            linkOrSkip(tempDir.resolve("avatar.jpg.meta"), outsideFile);

            StorageDownloadResult.InlineResult inline =
                    (StorageDownloadResult.InlineResult) adapter.resolveDownload("avatar.jpg", 60);

            assertEquals("image/jpeg", inline.contentType());
            assertEquals("image/jpeg", adapter.getMetadata("avatar.jpg").contentType());
        }

        @Test
        @DisplayName("should not read a content type sidecar linked inside the root")
        void shouldRejectSidecarReadThroughInRootLink() throws Exception {
            byte[] content = "bytes".getBytes();
            Files.write(tempDir.resolve("avatar.jpg"), content);
            Files.write(tempDir.resolve("planted"), "contentType=text/html".getBytes());
            linkOrSkip(tempDir.resolve("avatar.jpg.meta"), tempDir.resolve("planted"));

            StorageDownloadResult.InlineResult inline =
                    (StorageDownloadResult.InlineResult) adapter.resolveDownload("avatar.jpg", 60);

            assertEquals("image/jpeg", inline.contentType());
        }

        @Test
        @DisplayName("should reject a link even when it stays inside the root")
        void shouldRejectLinkInsideRoot() {
            byte[] content = "inside bytes".getBytes();
            adapter.store(
                    new FileUpload("real.jpg", "image/jpeg", content, content.length), "real.jpg");
            linkOrSkip(tempDir.resolve("alias.jpg"), tempDir.resolve("real.jpg"));

            // One rule for the whole tree: nothing stored locally is written by anything but this
            // adapter, so a link is a plant regardless of where it points.
            assertThrows(
                    FileValidationException.class, () -> adapter.resolveDownload("alias.jpg", 60));
            assertFalse(adapter.exists("alias.jpg"));
        }

        @Test
        @DisplayName("documents that hard links are outside the containment guarantee")
        void hardLinkIsServed_documentedTrustBoundary() throws Exception {
            try {
                Files.createLink(tempDir.resolve("public.jpg"), outsideFile);
            } catch (IOException | UnsupportedOperationException e) {
                assumeTrue(false, "hard links unsupported here: " + e.getMessage());
            }

            StorageDownloadResult.InlineResult inline =
                    (StorageDownloadResult.InlineResult) adapter.resolveDownload("public.jpg", 60);

            // A hard link is an ordinary directory entry: no no-follow open or real-path check can
            // tell it from the file it names. Asserted so the documented boundary in
            // ContainedFileTree stays visible rather than drifting into an assumed guarantee.
            assertArrayEquals("host secret".getBytes(), inline.content());
        }
    }

    @Nested
    @DisplayName("entries swapped during an operation")
    class ConcurrentEntrySwapTests {

        private static final int ATTEMPTS = 300;

        private Path outsideFile;

        @BeforeEach
        void createOutsideTarget(@TempDir Path outsideDir) throws Exception {
            outsideFile = outsideDir.resolve("secret.txt");
            Files.write(outsideFile, "host secret".getBytes());
        }

        private void assumeSymlinksSupported() {
            Path probe = tempDir.resolve("probe-link");
            try {
                Files.createSymbolicLink(probe, outsideFile);
                Files.delete(probe);
            } catch (IOException | UnsupportedOperationException e) {
                assumeTrue(false, "symbolic links unsupported here: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("should not write through a link planted after the target is checked")
        void shouldNotWriteThroughSwappedTarget() throws Exception {
            assumeSymlinksSupported();
            Path target = tempDir.resolve("payload.jpg");
            byte[] content = "planted upload".getBytes();
            FileUpload upload =
                    new FileUpload("payload.jpg", "image/jpeg", content, content.length);

            Thread swapper = startSwapper(() -> toggleLink(target, outsideFile));
            try {
                for (int i = 0; i < ATTEMPTS; i++) {
                    try {
                        adapter.store(upload, "payload.jpg");
                    } catch (FileStorageException e) {
                        // Expected whenever the link is in place when the write is attempted.
                    }
                }
            } finally {
                stop(swapper);
            }

            assertArrayEquals("host secret".getBytes(), Files.readAllBytes(outsideFile));
        }

        @Test
        @DisplayName("should not write through a directory swapped after the target is checked")
        void shouldNotWriteThroughSwappedDirectory() throws Exception {
            assumeSymlinksSupported();
            Path directory = tempDir.resolve("profiles");
            Path outsideDir = outsideFile.getParent();
            byte[] content = "planted upload".getBytes();
            FileUpload upload =
                    new FileUpload("payload.jpg", "image/jpeg", content, content.length);

            Thread swapper = startSwapper(() -> toggleDirectoryLink(directory, outsideDir));
            try {
                for (int i = 0; i < ATTEMPTS; i++) {
                    try {
                        adapter.store(upload, "profiles/payload.jpg");
                    } catch (FileStorageException e) {
                        // Expected whenever the link is in place when the write is attempted.
                    }
                }
            } finally {
                stop(swapper);
            }

            assertFalse(Files.exists(outsideDir.resolve("payload.jpg"), LinkOption.NOFOLLOW_LINKS));
            assertArrayEquals("host secret".getBytes(), Files.readAllBytes(outsideFile));
        }

        @Test
        @DisplayName("should not read through a link planted after the target is checked")
        void shouldNotReadThroughSwappedTarget() throws Exception {
            assumeSymlinksSupported();
            Path target = tempDir.resolve("payload.jpg");
            byte[] content = "stored bytes".getBytes();

            Thread swapper = startSwapper(() -> toggleFileOrLink(target, outsideFile, content));
            try {
                for (int i = 0; i < ATTEMPTS; i++) {
                    try {
                        StorageDownloadResult.InlineResult inline =
                                (StorageDownloadResult.InlineResult)
                                        adapter.resolveDownload("payload.jpg", 60);
                        // A partial read of the file being rewritten is fine; the external
                        // target's bytes must never come back.
                        assertFalse(Arrays.equals("host secret".getBytes(), inline.content()));
                    } catch (FileValidationException | StoredFileNotFoundException e) {
                        // Expected whenever the link or the gap is what the read finds.
                    }
                }
            } finally {
                stop(swapper);
            }
        }

        private Thread startSwapper(Runnable swap) {
            Thread thread =
                    new Thread(
                            () -> {
                                while (!Thread.currentThread().isInterrupted()) {
                                    swap.run();
                                }
                            });
            thread.setDaemon(true);
            thread.start();
            return thread;
        }

        private void stop(Thread swapper) throws Exception {
            swapper.interrupt();
            swapper.join(5_000);
        }

        private void toggleLink(Path link, Path target) {
            try {
                Files.deleteIfExists(link);
                Files.createSymbolicLink(link, target);
                Files.deleteIfExists(link);
            } catch (IOException e) {
                // The adapter is racing the same entry; losing a step is the point.
            }
        }

        private void toggleFileOrLink(Path entry, Path target, byte[] content) {
            try {
                Files.deleteIfExists(entry);
                Files.write(entry, content);
                Files.deleteIfExists(entry);
                Files.createSymbolicLink(entry, target);
            } catch (IOException e) {
                // The adapter is racing the same entry; losing a step is the point.
            }
        }

        private void toggleDirectoryLink(Path entry, Path target) {
            try {
                if (Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS)) {
                    try (var children = Files.list(entry)) {
                        for (Path child : children.toList()) {
                            Files.deleteIfExists(child);
                        }
                    }
                }
                Files.deleteIfExists(entry);
                Files.createSymbolicLink(entry, target);
                Files.deleteIfExists(entry);
            } catch (IOException e) {
                // The adapter is racing the same entry; losing a step is the point.
            }
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
