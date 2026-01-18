package com.iyte_yazilim.proje_pazari.presentation.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class ProjectSecurityCheckerTest {

    @Mock private ProjectRepository projectRepository;

    @Mock private UserDetails userDetails;

    @InjectMocks private ProjectSecurityChecker projectSecurityChecker;

    private final String PROJECT_ID = "proj-123";
    private final String USER_EMAIL = "owner@test.com";

    @Test
    @DisplayName("Should return true when user is the actual owner")
    void isOwner_ValidOwner_ReturnsTrue() {
        // Arrange
        UserEntity owner = new UserEntity();
        owner.setEmail(USER_EMAIL);
        ProjectEntity project = new ProjectEntity();
        project.setOwner(owner);

        when(userDetails.getUsername()).thenReturn(USER_EMAIL);
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));

        // Act
        boolean result = projectSecurityChecker.isOwner(PROJECT_ID, userDetails);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when user is not the owner")
    void isOwner_DifferentUser_ReturnsFalse() {
        // Arrange
        UserEntity owner = new UserEntity();
        owner.setEmail("other@test.com");
        ProjectEntity project = new ProjectEntity();
        project.setOwner(owner);

        when(userDetails.getUsername()).thenReturn(USER_EMAIL);
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));

        // Act
        boolean result = projectSecurityChecker.isOwner(PROJECT_ID, userDetails);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when project does not exist")
    void isOwner_ProjectNotFound_ReturnsFalse() {
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.empty());

        boolean result = projectSecurityChecker.isOwner(PROJECT_ID, userDetails);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when UserDetails is null")
    void isOwner_NullUserDetails_ReturnsFalse() {
        boolean result = projectSecurityChecker.isOwner(PROJECT_ID, null);

        assertFalse(result);
        verifyNoInteractions(projectRepository);
    }

    @Test
    @DisplayName("Should return false when project owner or email is null")
    void isOwner_NullProjectOwner_ReturnsFalse() {
        ProjectEntity project = new ProjectEntity(); // Owner is null
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));

        boolean result = projectSecurityChecker.isOwner(PROJECT_ID, userDetails);

        assertFalse(result);
    }
}
