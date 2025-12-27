package com.iyte_yazilim.proje_pazari.infrastructure.services;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final Path fileStorageLocation;

    public String storeFile(MultipartFile file, String userId) throws IOException {
        // Validate file type (only images)
        if (!isImageFile(file)) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        // Validate file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }

        // Get and validate original filename
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("File must have a valid filename");
        }
        
        originalFilename = StringUtils.cleanPath(originalFilename);
        String extension = "";
        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = userId + "_" + System.currentTimeMillis() + extension;

        // Save file
        Path targetLocation = fileStorageLocation.resolve(fileName).normalize();
        
        // Verify the normalized path is still within the storage location
        if (!targetLocation.startsWith(fileStorageLocation)) {
            throw new IllegalArgumentException("Invalid file path");
        }
        
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    public Resource loadFileAsResource(String fileName) throws IOException {
        Path filePath = fileStorageLocation.resolve(fileName).normalize();
        
        // Verify the normalized path is still within the storage location
        if (!filePath.startsWith(fileStorageLocation)) {
            throw new FileNotFoundException("Invalid file path");
        }
        
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists()) {
            return resource;
        } else {
            throw new FileNotFoundException("File not found: " + fileName);
        }
    }

    public void deleteFile(String fileName) throws IOException {
        Path filePath = fileStorageLocation.resolve(fileName).normalize();
        
        // Verify the normalized path is still within the storage location
        if (!filePath.startsWith(fileStorageLocation)) {
            throw new IOException("Invalid file path");
        }
        
        Files.deleteIfExists(filePath);
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        
        // Check content type
        if (contentType == null || !contentType.startsWith("image/")) {
            return false;
        }
        
        // Also validate file extension as additional security measure
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String lowerFilename = originalFilename.toLowerCase();
            boolean hasValidExtension = lowerFilename.endsWith(".jpg") || 
                                       lowerFilename.endsWith(".jpeg") || 
                                       lowerFilename.endsWith(".png") || 
                                       lowerFilename.endsWith(".gif") || 
                                       lowerFilename.endsWith(".webp");
            if (!hasValidExtension) {
                return false;
            }
        }
        
        return true;
    }
}
