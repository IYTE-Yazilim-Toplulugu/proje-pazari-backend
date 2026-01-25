package com.iyte_yazilim.proje_pazari.infrastructure.storage;

import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IFileStorageAdapter;
import com.iyte_yazilim.proje_pazari.domain.models.FileMetadata;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.http.Method;
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

    private final MinioClient minioClient;
    private final String bucketName;

    @Autowired
    public MinioStorageAdapter(
            @Value("${minio.url}") String url,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey,
            @Value("${minio.bucket-name}") String bucketName) {

        this.bucketName = bucketName;
        this.minioClient =
                MinioClient.builder().endpoint(url).credentials(accessKey, secretKey).build();

        createBucketIfNotExists();
    }

    /**
     * Package-private constructor for unit testing with a mock MinioClient.
     *
     * @param minioClient the MinIO client (can be mocked)
     * @param bucketName the bucket name
     */
    MinioStorageAdapter(MinioClient minioClient, String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    private void createBucketIfNotExists() {
        try {
            boolean exists =
                    minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Created MinIO bucket: {}", bucketName);
            } else {
                log.info("MinIO bucket already exists: {}", bucketName);
            }
        } catch (Exception e) {
            // Log warning instead of throwing to allow Spring context to load
            // The actual operations will fail later if MinIO is not available
            log.warn(
                    "Failed to verify/create MinIO bucket '{}'. "
                            + "MinIO may not be available. Operations will fail at runtime if MinIO is not accessible. "
                            + "Error: {}",
                    bucketName,
                    e.getMessage());
            // In test environments, we don't want to fail context loading
            // The actual storage operations will handle the errors appropriately
        }
    }

    @Override
    public String store(MultipartFile file, String path) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(path).stream(
                                    file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            log.debug("Stored file in MinIO: {}/{}", bucketName, path);

            return path;

            // Return the permanent object path; presigned URLs should be generated on-demand
            // using generatePresignedUrl when temporary access is needed.

        } catch (Exception e) {
            throw new FileStorageException("Failed to upload file to MinIO", e);
        }
    }

    @Override
    public String generatePresignedUrl(String path, int expirationMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(path)
                            .expiry(expirationMinutes, TimeUnit.MINUTES)
                            .build());
        } catch (Exception e) {
            throw new FileStorageException("Failed to generate presigned URL", e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucketName).object(path).build());
            log.debug("Deleted file from MinIO: {}/{}", bucketName, path);
        } catch (Exception e) {
            throw new FileStorageException("Failed to delete file from MinIO", e);
        }
    }

    @Override
    public boolean exists(String path) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucketName).object(path).build());
            return true;
        } catch (io.minio.errors.ErrorResponseException e) {
            // File does not exist - this is expected, not an error
            log.debug("File does not exist in MinIO: {}/{}", bucketName, path);
            return false;
        } catch (Exception e) {
            // Actual error (network, auth, etc.) - log as warning
            log.warn("Error checking if file exists in MinIO: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public FileMetadata getMetadata(String path) {
        try {
            StatObjectResponse stat =
                    minioClient.statObject(
                            StatObjectArgs.builder().bucket(bucketName).object(path).build());

            return new FileMetadata(
                    path,
                    stat.size(),
                    stat.contentType(),
                    stat.lastModified().toInstant(),
                    stat.lastModified().toInstant(),
                    path.substring(path.lastIndexOf("/") + 1),
                    stat.etag());
        } catch (Exception e) {
            throw new FileStorageException("Failed to get file metadata", e);
        }
    }
}
