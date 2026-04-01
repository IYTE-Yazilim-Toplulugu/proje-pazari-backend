package com.iyte_yazilim.proje_pazari.application.commands.uploadFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.services.FileStorageService;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class UploadFileHandlerTest {

    @Mock private FileStorageService fileStorageService;

    @Mock private MultipartFile mockFile;

    @InjectMocks private UploadFileHandler handler;

    @Test
    @DisplayName("Should return bad request when file is null")
    void handle_nullFile_returnsBadRequest() {
        UploadFileCommand command = new UploadFileCommand(null);

        ApiResponse<Map<String, Object>> response = handler.handle(command);

        assertEquals(ResponseCode.BAD_REQUEST, response.getCode());
        assertEquals("File is empty", response.getMessage());
        verifyNoInteractions(fileStorageService);
    }

    @Test
    @DisplayName("Should return bad request when file is empty")
    void handle_emptyFile_returnsBadRequest() {
        when(mockFile.isEmpty()).thenReturn(true);
        UploadFileCommand command = new UploadFileCommand(mockFile);

        ApiResponse<Map<String, Object>> response = handler.handle(command);

        assertEquals(ResponseCode.BAD_REQUEST, response.getCode());
        assertEquals("File is empty", response.getMessage());
        verifyNoInteractions(fileStorageService);
    }

    @Test
    @DisplayName("Should return bad request when filename is null")
    void handle_nullFilename_returnsBadRequest() {
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn(null);
        UploadFileCommand command = new UploadFileCommand(mockFile);

        ApiResponse<Map<String, Object>> response = handler.handle(command);

        assertEquals(ResponseCode.BAD_REQUEST, response.getCode());
        assertEquals("Invalid filename", response.getMessage());
        verifyNoInteractions(fileStorageService);
    }

    @Test
    @DisplayName("Should return bad request when filename is blank")
    void handle_blankFilename_returnsBadRequest() {
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("   ");
        UploadFileCommand command = new UploadFileCommand(mockFile);

        ApiResponse<Map<String, Object>> response = handler.handle(command);

        assertEquals(ResponseCode.BAD_REQUEST, response.getCode());
        assertEquals("Invalid filename", response.getMessage());
        verifyNoInteractions(fileStorageService);
    }

    @Test
    @DisplayName("Should return bad request when filename contains path traversal")
    void handle_pathTraversalFilename_returnsBadRequest() {
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("../etc/passwd");
        UploadFileCommand command = new UploadFileCommand(mockFile);

        ApiResponse<Map<String, Object>> response = handler.handle(command);

        assertEquals(ResponseCode.BAD_REQUEST, response.getCode());
        assertEquals("Invalid filename", response.getMessage());
        verifyNoInteractions(fileStorageService);
    }

    @Test
    @DisplayName("Should return success with file metadata on valid upload")
    void handle_validFile_returnsSuccess() {
        String filename = "document.pdf";
        String storedPath = "uploads/document.pdf";
        long fileSize = 1024L;

        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn(filename);
        when(mockFile.getSize()).thenReturn(fileSize);
        when(fileStorageService.storeFile(mockFile, "uploads")).thenReturn(storedPath);

        UploadFileCommand command = new UploadFileCommand(mockFile);
        ApiResponse<Map<String, Object>> response = handler.handle(command);

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("File uploaded successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(filename, response.getData().get("filename"));
        assertEquals("/api/v1/files/" + storedPath, response.getData().get("url"));
        assertEquals(fileSize, response.getData().get("size"));
        verify(fileStorageService).storeFile(mockFile, "uploads");
    }

    @Test
    @DisplayName("Should return error when file storage throws FileStorageException")
    void handle_storageException_returnsError() {
        String filename = "document.pdf";
        String errorMessage = "Disk full";

        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn(filename);
        when(fileStorageService.storeFile(mockFile, "uploads"))
                .thenThrow(new FileStorageException(errorMessage));

        UploadFileCommand command = new UploadFileCommand(mockFile);
        ApiResponse<Map<String, Object>> response = handler.handle(command);

        assertEquals(ResponseCode.INTERNAL_SERVER_ERROR, response.getCode());
        assertEquals("File upload failed: " + errorMessage, response.getMessage());
    }
}
