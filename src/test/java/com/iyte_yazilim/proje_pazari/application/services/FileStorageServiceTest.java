package com.iyte_yazilim.proje_pazari.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IFileStorageAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.unit.DataSize;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @Mock private IFileStorageAdapter storageAdapter;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(storageAdapter);

        ReflectionTestUtils.setField(fileStorageService, "maxFileSize", DataSize.ofMegabytes(10));
        ReflectionTestUtils.setField(fileStorageService, "allowedContentTypesString", "image/png,application/pdf");
        ReflectionTestUtils.setField(fileStorageService, "avatarsBucket", "proje-pazari-avatars");
        ReflectionTestUtils.setField(fileStorageService, "documentsBucket", "proje-pazari-documents");
    }

    @Test
    void shouldStoreUserAvatarWithOrganizedPath() {
        MockMultipartFile file =
                new MockMultipartFile("file", "avatar.png", "image/png", "img".getBytes());
        String expectedPath = "proje-pazari-avatars/users/user-1/avatar.png";

        when(storageAdapter.store(any(), eq(expectedPath))).thenReturn(expectedPath);

        String storedPath = fileStorageService.storeUserAvatar("user-1", file);

        assertEquals(expectedPath, storedPath);
        verify(storageAdapter).store(any(), eq(expectedPath));
    }

    @Test
    void shouldStoreProjectDocumentWithOrganizedPath() {
        MockMultipartFile file =
                new MockMultipartFile("file", "spec.pdf", "application/pdf", "pdf".getBytes());
        String expectedPath = "proje-pazari-documents/projects/proj-1/doc-1.pdf";

        when(storageAdapter.store(any(), eq(expectedPath))).thenReturn(expectedPath);

        String storedPath = fileStorageService.storeProjectDocument("proj-1", "doc-1", file);

        assertEquals(expectedPath, storedPath);
        verify(storageAdapter).store(any(), eq(expectedPath));
    }

    @Test
    void shouldRejectInvalidUserIdForAvatarPath() {
        MockMultipartFile file =
                new MockMultipartFile("file", "avatar.png", "image/png", "img".getBytes());

        assertThrows(
                FileStorageException.class,
                () -> fileStorageService.storeUserAvatar("../bad", file));
    }
}
