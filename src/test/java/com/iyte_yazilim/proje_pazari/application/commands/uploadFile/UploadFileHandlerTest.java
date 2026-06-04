package com.iyte_yazilim.proje_pazari.application.commands.uploadFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ResponseCode;
import com.iyte_yazilim.proje_pazari.application.services.FileStorageService;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FileValidationException;
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
    @DisplayName("Should throw FileValidationException when file is null")
    void handle_nullFile_throwsFileValidationException() {
        UploadFileCommand command = new UploadFileCommand(null);
        assertThrows(FileValidationException.class, () -> handler.handle(command));
        verifyNoInteractions(fileStorageService);
    }

    @Test
    @DisplayName("Should throw FileValidationException when file is empty")
    void handle_emptyFile_throwsFileValidationException() {
        when(mockFile.isEmpty()).thenReturn(true);
        UploadFileCommand command = new UploadFileCommand(mockFile);
        assertThrows(FileValidationException.class, () -> handler.handle(command));
        verifyNoInteractions(fileStorageService);
    }

    @Test
    @DisplayName("Should throw FileValidationException when filename is null")
    void handle_nullFilename_throwsFileValidationException() {
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn(null);
        UploadFileCommand command = new UploadFileCommand(mockFile);
        assertThrows(FileValidationException.class, () -> handler.handle(command));
        verifyNoInteractions(fileStorageService);
    }

    @Test
    @DisplayName("Should throw FileValidationException when filename is blank")
    void handle_blankFilename_throwsFileValidationException() {
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("   ");
        UploadFileCommand command = new UploadFileCommand(mockFile);
        assertThrows(FileValidationException.class, () -> handler.handle(command));
        verifyNoInteractions(fileStorageService);
    }

    @Test
    @DisplayName("Should throw FileValidationException when filename contains path traversal")
    void handle_pathTraversalFilename_throwsFileValidationException() {
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("../etc/passwd");
        UploadFileCommand command = new UploadFileCommand(mockFile);
        assertThrows(FileValidationException.class, () -> handler.handle(command));
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
        assertNotNull(response.getData());
        assertEquals(filename, response.getData().get("filename"));
        assertEquals("/api/v1/files/" + storedPath, response.getData().get("url"));
        assertEquals(fileSize, response.getData().get("size"));
        verify(fileStorageService).storeFile(mockFile, "uploads");
    }

    @Test
    @DisplayName("Should propagate FileStorageException when storage fails")
    void handle_storageException_propagatesFileStorageException() {
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("document.pdf");
        when(fileStorageService.storeFile(mockFile, "uploads"))
                .thenThrow(new FileStorageException("Disk full"));

        UploadFileCommand command = new UploadFileCommand(mockFile);
        assertThrows(FileStorageException.class, () -> handler.handle(command));
    }
}
