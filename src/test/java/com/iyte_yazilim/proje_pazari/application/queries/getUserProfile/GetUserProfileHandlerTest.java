package com.iyte_yazilim.proje_pazari.application.queries.getUserProfile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.dtos.UserProfileDTO;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetUserProfileHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private GetUserProfileHandler handler;

    @BeforeEach
    void setUp() {
        lenient()
                .when(messageService.getMessage("user.retrieved.success"))
                .thenReturn("User retrieved successfully");
        lenient()
                .when(messageService.getMessage(eq("user.not.found"), any(Object[].class)))
                .thenAnswer(
                        invocation -> {
                            Object[] args = invocation.getArgument(1);
                            return "User " + args[0] + " not found";
                        });
    }

    @Test
    @DisplayName("Should return user profile with projects")
    void shouldReturnUserProfile_whenUserExists() {
        // Given
        String userId = "user-123";
        GetUserProfileQuery query = new GetUserProfileQuery(userId);

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setEmail("test@std.iyte.edu.tr");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setDescription("Developer");
        user.setCreatedAt(LocalDateTime.of(2024, 1, 1, 0, 0));

        ProjectEntity project = new ProjectEntity();
        project.setId("proj-1");
        project.setTitle("Test Project");
        project.setDescription("A test project");
        project.setStatus(ProjectStatus.DRAFT);
        project.setCreatedAt(LocalDateTime.of(2024, 2, 1, 0, 0));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.countProjectsByUserId(userId)).thenReturn(1);
        when(userRepository.countApplicationsByUserId(userId)).thenReturn(2);
        when(projectRepository.findByOwnerId(userId)).thenReturn(List.of(project));

        // When
        ApiResponse<UserProfileDTO> response = handler.handle(query);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertNotNull(response.getData());
        assertEquals(userId, response.getData().id());
        assertEquals("test@std.iyte.edu.tr", response.getData().email());
        assertEquals("John", response.getData().firstName());
        assertEquals("Doe", response.getData().lastName());
        assertEquals("John Doe", response.getData().fullName());
        assertEquals(1, response.getData().projectsCreated());
        assertEquals(2, response.getData().applicationsSubmitted());
        assertEquals(1, response.getData().projects().size());
        assertEquals("Test Project", response.getData().projects().get(0).title());
    }

    @Test
    @DisplayName("Should return not found when user does not exist")
    void shouldReturnNotFound_whenUserDoesNotExist() {
        // Given
        String userId = "nonexistent-user";
        GetUserProfileQuery query = new GetUserProfileQuery(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        ApiResponse<UserProfileDTO> response = handler.handle(query);

        // Then
        assertEquals(ResponseCode.NOT_FOUND, response.getCode());
        assertTrue(response.getMessage().contains("not found"));
        assertNull(response.getData());
        verify(projectRepository, never()).findByOwnerId(any());
    }

    @Test
    @DisplayName("Should return profile with empty projects list")
    void shouldReturnProfile_withEmptyProjects() {
        // Given
        String userId = "user-456";
        GetUserProfileQuery query = new GetUserProfileQuery(userId);

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setEmail("student@std.iyte.edu.tr");
        user.setFirstName("Jane");
        user.setLastName("Smith");
        user.setCreatedAt(LocalDateTime.of(2024, 3, 1, 0, 0));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.countProjectsByUserId(userId)).thenReturn(0);
        when(userRepository.countApplicationsByUserId(userId)).thenReturn(0);
        when(projectRepository.findByOwnerId(userId)).thenReturn(Collections.emptyList());

        // When
        ApiResponse<UserProfileDTO> response = handler.handle(query);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertNotNull(response.getData());
        assertEquals(0, response.getData().projectsCreated());
        assertTrue(response.getData().projects().isEmpty());
    }
}
