package com.iyte_yazilim.proje_pazari.application.commands.uploadProfilePicture;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AvatarPathResolverTest {

    @Test
    @DisplayName("Should return null when the stored value is null")
    void shouldReturnNull_whenStoredValueIsNull() {
        assertNull(AvatarPathResolver.resolveStoragePath(null));
    }

    @Test
    @DisplayName("Should return a bare bucket-prefixed MinIO path unchanged")
    void shouldResolve_bareBucketPrefixedPath() {
        String path = "proje-pazari-avatars/users/user-1/avatar-01ABC.jpg";
        assertEquals(path, AvatarPathResolver.resolveStoragePath(path));
    }

    @Test
    @DisplayName("Should strip the /api/v1/files/ prefix from an API-relative path")
    void shouldResolve_apiRelativePath() {
        String stored = "/api/v1/files/proje-pazari-avatars/users/user-1/avatar-01ABC.jpg";
        assertEquals(
                "proje-pazari-avatars/users/user-1/avatar-01ABC.jpg",
                AvatarPathResolver.resolveStoragePath(stored));
    }

    @Test
    @DisplayName("Should extract the bucket + object path from an absolute/presigned URL")
    void shouldResolve_absolutePresignedUrl() {
        String stored =
                "http://minio:9000/proje-pazari-avatars/users/user-1/avatar-01ABC.jpg"
                        + "?X-Amz-Expires=600&X-Amz-Signature=abc";
        assertEquals(
                "proje-pazari-avatars/users/user-1/avatar-01ABC.jpg",
                AvatarPathResolver.resolveStoragePath(stored));
    }

    @Test
    @DisplayName("Should return a legacy bare profiles/ path unchanged")
    void shouldResolve_legacyProfilesPath() {
        String path = "profiles/old-ulid.jpg";
        assertEquals(path, AvatarPathResolver.resolveStoragePath(path));
    }

    @Test
    @DisplayName("Should return null when an absolute URL has no meaningful path to extract")
    void shouldReturnNull_whenUrlHasNoExtractablePath() {
        assertNull(AvatarPathResolver.resolveStoragePath("http://minio:9000"));
    }
}
