package com.iyte_yazilim.proje_pazari.infrastructure.storage;

import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IFileStorageAdapter;
import com.iyte_yazilim.proje_pazari.domain.models.FileMetadata;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * MinIO implementation for local development and self-hosted deployments. Active when
 * storage.provider=minio
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "storage.provider", havingValue = "minio", matchIfMissing = true)
public class MinioStorageAdapter implements IFileStorageAdapter {

    private static final String DEFAULT_AVATARS_BUCKET = "proje-pazari-avatars";
    private static final String DEFAULT_DOCUMENTS_BUCKET = "proje-pazari-documents";
    private static final String DEFAULT_BACKUPS_BUCKET = "proje-pazari-backups";

    private final MinioClient minioClient;
    private final String bucketName;
    private final Set<String> configuredBuckets;

    @Autowired
    public MinioStorageAdapter(
            @Value("${minio.url}") String url,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey,
            @Value("${minio.bucket-name}") String bucketName,
            @Value("${minio.avatars-bucket:" + DEFAULT_AVATARS_BUCKET + "}")
                    String avatarsBucketName,
            @Value("${minio.documents-bucket:" + DEFAULT_DOCUMENTS_BUCKET + "}")
                    String documentsBucketName,
            @Value("${minio.backups-bucket:" + DEFAULT_BACKUPS_BUCKET + "}")
                    String backupsBucketName) {
        this(
                MinioClient.builder().endpoint(url).credentials(accessKey, secretKey).build(),
                bucketName,
                avatarsBucketName,
                documentsBucketName,
                backupsBucketName,
                true);
    }

    public MinioStorageAdapter(String url, String accessKey, String secretKey, String bucketName) {
        this(
                MinioClient.builder().endpoint(url).credentials(accessKey, secretKey).build(),
                bucketName,
                DEFAULT_AVATARS_BUCKET,
                DEFAULT_DOCUMENTS_BUCKET,
                DEFAULT_BACKUPS_BUCKET,
                true);
    }

    /**
     * Package-private constructor for unit testing with a mock MinioClient.
     *
     * @param minioClient the MinIO client (can be mocked)
     * @param bucketName the default bucket name
     */
    MinioStorageAdapter(MinioClient minioClient, String bucketName) {
        this(
                minioClient,
                bucketName,
                DEFAULT_AVATARS_BUCKET,
                DEFAULT_DOCUMENTS_BUCKET,
                DEFAULT_BACKUPS_BUCKET,
                false);
    }

    MinioStorageAdapter(
            MinioClient minioClient,
            String bucketName,
            String avatarsBucketName,
            String documentsBucketName,
            String backupsBucketName) {
        this(
                minioClient,
                bucketName,
                avatarsBucketName,
                documentsBucketName,
                backupsBucketName,
                false);
    }

    private MinioStorageAdapter(
            MinioClient minioClient,
            String bucketName,
            String avatarsBucketName,
            String documentsBucketName,
            String backupsBucketName,
            boolean initializeBuckets) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;

        LinkedHashSet<String> buckets = new LinkedHashSet<>();
        buckets.add(bucketName);
        buckets.add(avatarsBucketName);
        buckets.add(documentsBucketName);
        buckets.add(backupsBucketName);
        this.configuredBuckets = Set.copyOf(buckets);

        if (initializeBuckets) {
            createBucketsIfNotExists();
        }
    }

    private void createBucketsIfNotExists() {
        for (String bucket : configuredBuckets) {
            createBucketIfNotExists(bucket);
        }
    }

    private void createBucketIfNotExists(String bucket) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Created MinIO bucket: {}", bucket);
            } else {
                log.info("MinIO bucket already exists: {}", bucket);
            }
        } catch (Exception e) {
            // Do not fail Spring context load; runtime operations will surface errors if storage is
            // unreachable.
            log.warn(
                    "Failed to verify/create MinIO bucket '{}'. "
                            + "MinIO may not be available. Error: {}",
                    bucket,
                    e.getMessage());
        }
    }

    @Override
    public String store(MultipartFile file, String path) {
        StorageLocation location = resolveLocation(path);
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(location.bucket())
                            .object(location.objectPath())
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            log.debug("Stored file in MinIO: {}/{}", location.bucket(), location.objectPath());
            return location.asStoragePath();
        } catch (Exception e) {
            throw new FileStorageException("Failed to upload file to MinIO", e);
        }
    }

    @Override
    public String generatePresignedUrl(String path, int expirationMinutes) {
        StorageLocation location = resolveLocation(path);
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(location.bucket())
                            .object(location.objectPath())
                            .expiry(expirationMinutes, TimeUnit.MINUTES)
                            .build());
        } catch (Exception e) {
            throw new FileStorageException("Failed to generate presigned URL", e);
        }
    }

    @Override
    public void delete(String path) {
        StorageLocation location = resolveLocation(path);
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(location.bucket())
                            .object(location.objectPath())
                            .build());
            log.debug("Deleted file from MinIO: {}/{}", location.bucket(), location.objectPath());
        } catch (Exception e) {
            throw new FileStorageException("Failed to delete file from MinIO", e);
        }
    }

    @Override
    public boolean exists(String path) {
        StorageLocation location = resolveLocation(path);
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(location.bucket())
                            .object(location.objectPath())
                            .build());
            return true;
        } catch (io.minio.errors.ErrorResponseException e) {
            log.debug("File does not exist in MinIO: {}/{}", location.bucket(), location.objectPath());
            return false;
        } catch (Exception e) {
            log.warn("Error checking if file exists in MinIO: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public FileMetadata getMetadata(String path) {
        StorageLocation location = resolveLocation(path);
        try {
            StatObjectResponse stat =
                    minioClient.statObject(
                            StatObjectArgs.builder()
                                    .bucket(location.bucket())
                                    .object(location.objectPath())
                                    .build());

            return new FileMetadata(
                    location.asStoragePath(),
                    stat.size(),
                    stat.contentType(),
                    stat.lastModified().toInstant(),
                    stat.lastModified().toInstant(),
                    location.fileName(),
                    stat.etag());
        } catch (Exception e) {
            throw new FileStorageException("Failed to get file metadata", e);
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            boolean defaultBucketExists =
                    minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!defaultBucketExists) {
                return false;
            }
            minioClient.listBuckets();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Long getTotalSpaceBytes() {
        return null;
    }

    @Override
    public Long getUsedSpaceBytes() {
        long usedBytes = 0L;
        try {
            for (String bucket : configuredBuckets) {
                if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                    continue;
                }

                Iterable<Result<Item>> objects =
                        minioClient.listObjects(
                                ListObjectsArgs.builder().bucket(bucket).recursive(true).build());
                for (Result<Item> result : objects) {
                    try {
                        usedBytes += result.get().size();
                    } catch (Exception itemError) {
                        log.warn(
                                "Failed to read object size while calculating usage for bucket '{}': {}",
                                bucket,
                                itemError.getMessage());
                    }
                }
            }
            return usedBytes;
        } catch (Exception e) {
            log.warn("Failed to calculate MinIO used space: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public List<String> listBuckets() {
        try {
            return minioClient.listBuckets().stream().map(Bucket::name).toList();
        } catch (Exception e) {
            log.warn("Failed to list MinIO buckets: {}", e.getMessage());
            return List.of();
        }
    }

    private StorageLocation resolveLocation(String rawPath) {
        String normalizedPath = normalizePath(rawPath);

        int firstSlashIndex = normalizedPath.indexOf('/');
        if (firstSlashIndex > 0) {
            String firstSegment = normalizedPath.substring(0, firstSlashIndex);
            String remainder = normalizedPath.substring(firstSlashIndex + 1);
            if (configuredBuckets.contains(firstSegment)) {
                if (remainder.isBlank()) {
                    throw new FileStorageException("Invalid file path");
                }
                return new StorageLocation(firstSegment, remainder, true);
            }
        }

        return new StorageLocation(bucketName, normalizedPath, false);
    }

    private String normalizePath(String path) {
        if (path == null) {
            throw new FileStorageException("Invalid file path");
        }

        String normalized = path.trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        if (normalized.isBlank()) {
            throw new FileStorageException("Invalid file path");
        }

        return normalized;
    }

    private record StorageLocation(String bucket, String objectPath, boolean includesBucketPrefix) {

        String asStoragePath() {
            if (includesBucketPrefix) {
                return bucket + "/" + objectPath;
            }
            return objectPath;
        }

        String fileName() {
            int lastSlash = objectPath.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash + 1 < objectPath.length()) {
                return objectPath.substring(lastSlash + 1);
            }
            return objectPath;
        }
    }
}
