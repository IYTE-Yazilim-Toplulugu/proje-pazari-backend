package com.iyte_yazilim.proje_pazari.application.commands.updateUserProfile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ResponseCode;
import com.iyte_yazilim.proje_pazari.application.dtos.UserDto;
import com.iyte_yazilim.proje_pazari.application.mappers.UserDtoMapper;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.events.UserUpdatedEvent;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class UpdateUserProfileHandlerTest {

    @Mock private IUserRepository userRepository;

    @Mock private UserDtoMapper userDtoMapper;

    @Mock private MessageService messageService;

    @Mock private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks private UpdateUserProfileHandler handler;

    @BeforeEach
    void setUp() {
        lenient()
                .when(messageService.getMessage("user.profile.updated"))
                .thenReturn("Profile updated successfully");
        lenient()
                .when(messageService.getMessage(eq("user.not.found.with.id"), any(Object[].class)))
                .thenAnswer(
                        invocation -> {
                            Object[] args = invocation.getArgument(1);
                            return "User with ID " + args[0] + " not found";
                        });
    }

    @Test
    @DisplayName("Should update profile successfully with all fields")
    void shouldUpdateProfile_whenAllFieldsProvided() {
        // Given
        String userId = Ulid.fast().toString();
        Ulid ulid = Ulid.from(userId);
        UpdateUserProfileCommand command =
                new UpdateUserProfileCommand(
                        userId,
                        "John",
                        "Doe",
                        "Software Engineer",
                        "https://www.linkedin.com/in/johndoe",
                        "https://github.com/johndoe",
                        "en");

        User user =
                new User("john@std.iyte.edu.tr", "hashedPassword", "OldFirstName", "OldLastName");
        user.setId(ulid);
        user.setPreferredLanguage("tr");

        UserDto expectedDto =
                new UserDto(
                        userId,
                        "john@std.iyte.edu.tr",
                        "John",
                        "Doe",
                        "Software Engineer",
                        null,
                        "https://www.linkedin.com/in/johndoe",
                        "https://github.com/johndoe",
                        "en");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userDtoMapper.domainToDto(user)).thenReturn(expectedDto);

        // When
        ApiResponse<UserDto> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("Profile updated successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("John", response.getData().firstName());
        assertEquals("Doe", response.getData().lastName());
        assertEquals("Software Engineer", response.getData().description());
        assertEquals("en", response.getData().preferredLanguage());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("en", user.getPreferredLanguage());
        verify(userRepository).save(user);
        verify(applicationEventPublisher).publishEvent(any(UserUpdatedEvent.class));
    }

    @Test
    @DisplayName("Should update only provided fields")
    void shouldUpdateProfile_whenPartialFieldsProvided() {
        // Given
        String userId = Ulid.fast().toString();
        Ulid ulid = Ulid.from(userId);
        UpdateUserProfileCommand command =
                new UpdateUserProfileCommand(userId, "NewFirstName", null, null, null, null, null);

        User user =
                new User("user@std.iyte.edu.tr", "hashedPassword", "OldFirstName", "OldLastName");
        user.setId(ulid);
        user.setDescription("Old description");
        user.setPreferredLanguage("tr");

        UserDto expectedDto =
                new UserDto(
                        userId,
                        "user@std.iyte.edu.tr",
                        "NewFirstName",
                        "OldLastName",
                        "Old description",
                        null,
                        null,
                        null,
                        "tr");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userDtoMapper.domainToDto(user)).thenReturn(expectedDto);

        // When
        ApiResponse<UserDto> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("NewFirstName", user.getFirstName());
        assertEquals("OldLastName", user.getLastName());
        assertEquals("Old description", user.getDescription());
        assertEquals("tr", user.getPreferredLanguage());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist")
    void shouldThrowException_whenUserNotFound() {
        // Given
        String nonExistentUserId = Ulid.fast().toString();
        UpdateUserProfileCommand command =
                new UpdateUserProfileCommand(
                        nonExistentUserId, "John", "Doe", null, null, null, null);

        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> handler.handle(command));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should set URL to null when blank URL provided")
    void shouldSetUrlToNull_whenBlankUrlProvided() {
        // Given
        String userId = Ulid.fast().toString();
        Ulid ulid = Ulid.from(userId);
        UpdateUserProfileCommand command =
                new UpdateUserProfileCommand(userId, null, null, null, "   ", "   ", null);

        User user = new User("user@std.iyte.edu.tr", "hashedPassword", "First", "Last");
        user.setId(ulid);
        user.setLinkedinUrl("https://www.linkedin.com/in/old");
        user.setGithubUrl("https://github.com/old");

        UserDto expectedDto = new UserDto(userId, null, null, null, null, null, null, null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userDtoMapper.domainToDto(user)).thenReturn(expectedDto);

        // When
        ApiResponse<UserDto> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertNull(user.getLinkedinUrl());
        assertNull(user.getGithubUrl());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should accept valid LinkedIn URL with www")
    void shouldAcceptValidLinkedInUrl_withWww() {
        // Given
        String userId = Ulid.fast().toString();
        Ulid ulid = Ulid.from(userId);
        UpdateUserProfileCommand command =
                new UpdateUserProfileCommand(
                        userId,
                        null,
                        null,
                        null,
                        "https://www.linkedin.com/in/johndoe",
                        null,
                        null);

        User user = new User("user@std.iyte.edu.tr", "hashedPassword", "First", "Last");
        user.setId(ulid);

        UserDto expectedDto =
                new UserDto(
                        userId,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "https://www.linkedin.com/in/johndoe",
                        null,
                        null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userDtoMapper.domainToDto(user)).thenReturn(expectedDto);

        // When
        ApiResponse<UserDto> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("https://www.linkedin.com/in/johndoe", user.getLinkedinUrl());
    }

    @Test
    @DisplayName("Should accept valid LinkedIn URL without www")
    void shouldAcceptValidLinkedInUrl_withoutWww() {
        // Given
        String userId = Ulid.fast().toString();
        Ulid ulid = Ulid.from(userId);
        UpdateUserProfileCommand command =
                new UpdateUserProfileCommand(
                        userId, null, null, null, "https://linkedin.com/in/johndoe", null, null);

        User user = new User("user@std.iyte.edu.tr", "hashedPassword", "First", "Last");
        user.setId(ulid);

        UserDto expectedDto =
                new UserDto(
                        userId,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "https://linkedin.com/in/johndoe",
                        null,
                        null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userDtoMapper.domainToDto(user)).thenReturn(expectedDto);

        // When
        ApiResponse<UserDto> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("https://linkedin.com/in/johndoe", user.getLinkedinUrl());
    }

    @Test
    @DisplayName("Should accept valid GitHub URL with path")
    void shouldAcceptValidGithubUrl_withPath() {
        // Given
        String userId = Ulid.fast().toString();
        Ulid ulid = Ulid.from(userId);
        UpdateUserProfileCommand command =
                new UpdateUserProfileCommand(
                        userId, null, null, null, null, "https://github.com/johndoe/my-repo", null);

        User user = new User("user@std.iyte.edu.tr", "hashedPassword", "First", "Last");
        user.setId(ulid);

        UserDto expectedDto =
                new UserDto(
                        userId,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "https://github.com/johndoe/my-repo",
                        null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userDtoMapper.domainToDto(user)).thenReturn(expectedDto);

        // When
        ApiResponse<UserDto> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("https://github.com/johndoe/my-repo", user.getGithubUrl());
    }

    @Test
    @DisplayName("Should update preferredLanguage when provided")
    void shouldUpdatePreferredLanguage_whenProvided() {
        // Given
        String userId = Ulid.fast().toString();
        Ulid ulid = Ulid.from(userId);
        UpdateUserProfileCommand command =
                new UpdateUserProfileCommand(userId, null, null, null, null, null, "en");

        User user = new User("user@std.iyte.edu.tr", "hashedPassword", "First", "Last");
        user.setId(ulid);
        user.setPreferredLanguage("tr");

        UserDto expectedDto =
                new UserDto(
                        userId,
                        "user@std.iyte.edu.tr",
                        "First",
                        "Last",
                        null,
                        null,
                        null,
                        null,
                        "en");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userDtoMapper.domainToDto(user)).thenReturn(expectedDto);

        // When
        ApiResponse<UserDto> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("en", user.getPreferredLanguage());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should not update preferredLanguage when null")
    void shouldNotUpdatePreferredLanguage_whenNull() {
        // Given
        String userId = Ulid.fast().toString();
        Ulid ulid = Ulid.from(userId);
        UpdateUserProfileCommand command =
                new UpdateUserProfileCommand(userId, "NewFirst", null, null, null, null, null);

        User user = new User("user@std.iyte.edu.tr", "hashedPassword", "First", "Last");
        user.setId(ulid);
        user.setPreferredLanguage("tr");

        UserDto expectedDto =
                new UserDto(
                        userId,
                        "user@std.iyte.edu.tr",
                        "NewFirst",
                        "Last",
                        null,
                        null,
                        null,
                        null,
                        "tr");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userDtoMapper.domainToDto(user)).thenReturn(expectedDto);

        // When
        ApiResponse<UserDto> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("tr", user.getPreferredLanguage());
        verify(userRepository).save(user);
    }
}
