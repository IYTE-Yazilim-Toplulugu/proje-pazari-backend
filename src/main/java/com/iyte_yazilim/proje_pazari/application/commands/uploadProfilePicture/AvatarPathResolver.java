package com.iyte_yazilim.proje_pazari.application.commands.uploadProfilePicture;

import java.net.URI;

/**
 * Resolves a persisted {@code profilePictureUrl} value back into the bare storage path that {@code
 * FileStorageService.deleteFile(String)} expects.
 *
 * <p>Existing users may carry a {@code profilePictureUrl} in any of several historical forms: a
 * bare MinIO bucket-prefixed path, an API-relative path ({@code /api/v1/files/...}), an
 * absolute/presigned URL, or a legacy {@code profiles/...} path. All of them must keep resolving
 * correctly with no data migration. This same resolution also applies to a freshly stored avatar's
 * returned reference, since {@code LocalStorageAdapter} returns an API-relative path that {@code
 * FileStorageService.deleteFile} would otherwise reject outright.
 */
public final class AvatarPathResolver {

    private AvatarPathResolver() {}

    public static String resolveStoragePath(String storedValue) {
        if (storedValue == null) {
            return null;
        }

        // Handle API path format: /api/v1/files/profiles/filename.jpg
        if (storedValue.contains("/api/v1/files/")) {
            return storedValue.substring(
                    storedValue.indexOf("/api/v1/files/") + "/api/v1/files/".length());
        }

        // Handle simple storage path format (e.g., "profiles/filename.jpg")
        // This is the path returned by the storage adapter's store() method
        if (!storedValue.startsWith("http") && !storedValue.startsWith("/api")) {
            return storedValue;
        }

        // Handle presigned URL format and keep bucket + object path.
        // Example: http://minio:9000/bucket-name/users/user-1/avatar.jpg?...
        try {
            URI uri = URI.create(storedValue.split("\\?")[0]);
            String path = uri.getPath();
            if (path != null && path.length() > 1) {
                return path.substring(1); // Remove leading slash only
            }
        } catch (IllegalArgumentException e) {
            // Fall back to original behavior if URL parsing fails
        }

        // Legacy fallback for /profiles/ pattern
        if (storedValue.contains("/profiles/")) {
            int profilesIndex = storedValue.indexOf("/profiles/");
            int queryIndex = storedValue.indexOf("?");
            if (queryIndex > profilesIndex) {
                return storedValue.substring(profilesIndex + 1, queryIndex);
            }
            return storedValue.substring(profilesIndex + 1);
        }

        return null;
    }
}
