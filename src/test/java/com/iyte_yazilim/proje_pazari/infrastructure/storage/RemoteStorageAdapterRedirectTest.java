package com.iyte_yazilim.proje_pazari.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IFileStorageAdapter;
import com.iyte_yazilim.proje_pazari.domain.models.FileMetadata;
import com.iyte_yazilim.proje_pazari.domain.models.FileUpload;
import com.iyte_yazilim.proje_pazari.domain.models.StorageDownloadResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Regression coverage for the redirect half of the local-download fix: adapters that CAN hand out a
 * presigned URL (MinIO/S3) must keep redirecting, via {@link IFileStorageAdapter}'s default {@code
 * resolveDownload}.
 *
 * <p>This uses a hand-written fake rather than a Mockito mock on purpose: Mockito does not execute
 * interface default methods, so a mock-based test would stub the very behavior under test and prove
 * nothing.
 */
class RemoteStorageAdapterRedirectTest {

    private static final String API_ROUTE_PREFIX = "/api/v1/files/";

    /** Minimal stand-in for a remote, presigned-URL-capable backend. */
    private static final class FakeRemoteStorageAdapter implements IFileStorageAdapter {

        private String requestedPath;
        private int requestedExpiryMinutes;

        @Override
        public String store(FileUpload file, String path) {
            throw new UnsupportedOperationException("not needed for this test");
        }

        @Override
        public String generatePresignedUrl(String path, int expirationMinutes) {
            this.requestedPath = path;
            this.requestedExpiryMinutes = expirationMinutes;
            return "https://minio.example.com/bucket/" + path + "?signed=true";
        }

        @Override
        public void delete(String path) {
            throw new UnsupportedOperationException("not needed for this test");
        }

        @Override
        public boolean exists(String path) {
            return true;
        }

        @Override
        public FileMetadata getMetadata(String path) {
            throw new UnsupportedOperationException("not needed for this test");
        }
    }

    @Test
    @DisplayName("Default resolveDownload wraps the presigned URL in a RedirectResult")
    void defaultResolveDownload_returnsRedirectResult() {
        FakeRemoteStorageAdapter adapter = new FakeRemoteStorageAdapter();

        StorageDownloadResult result = adapter.resolveDownload("profiles/test.jpg", 60);

        assertThat(result).isInstanceOf(StorageDownloadResult.RedirectResult.class);
        assertThat(((StorageDownloadResult.RedirectResult) result).url())
                .isEqualTo("https://minio.example.com/bucket/profiles/test.jpg?signed=true");
        assertThat(adapter.requestedPath).isEqualTo("profiles/test.jpg");
        assertThat(adapter.requestedExpiryMinutes).isEqualTo(60);
    }

    @Test
    @DisplayName("Remote redirect target never points back at this API's own download route")
    void defaultResolveDownload_doesNotRedirectToOwnApiRoute() {
        FakeRemoteStorageAdapter adapter = new FakeRemoteStorageAdapter();

        StorageDownloadResult result = adapter.resolveDownload("profiles/test.jpg", 60);
        String url = ((StorageDownloadResult.RedirectResult) result).url();

        // This is the exact failure mode of the bug: a Location header that resolves back to
        // /api/v1/files/** would make the client loop on the same endpoint forever.
        assertThat(url).doesNotStartWith(API_ROUTE_PREFIX);
        assertThat(java.net.URI.create(url).getPath()).doesNotStartWith(API_ROUTE_PREFIX);
    }

    @Test
    @DisplayName("MinioStorageAdapter inherits the redirect behavior rather than overriding it")
    void minioAdapter_doesNotOverrideResolveDownload() throws Exception {
        assertThat(MinioStorageAdapter.class.getDeclaredMethods())
                .noneMatch(method -> method.getName().equals("resolveDownload"));
    }
}
