package com.iyte_yazilim.proje_pazari.application.commands.updateUserProfile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.domain.events.UserUpdatedEvent;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.dtos.UserDto;
import com.iyte_yazilim.proje_pazari.application.mappers.UserDtoMapper;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
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

    @Mock private UserRepository userRepository;

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
        UpdateUserProfileCommand command =
                new UpdateUserProfileCommand(
                        userId,
                        "John",
                        "Doe",
                        "Software Engineer",
                        "https://www.linkedin.com/in/johndoe",
                        "https://github.com/johndoe",
                        "en");

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setEmail("john@std.iyte.edu.tr");
        userEntity.setFirstName("OldFirstName");
        userEntity.setLastName("OldLastName");

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

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userDtoMapper.toDto(userEntity)).thenReturn(expectedDto);

        // When
        ApiResponse<UserDto> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("Profile updated successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("John", response.getData().firstName());
        assertEquals("Doe", response.getData().lastName());
        assertEquals("Software Engineer", response.getData().description());
        verify(userRepository).save(userEntity);
        verify(applicationEventPublisher).publishEvent(any(UserUpdatedEvent.class));
    }

    @Test
    @DisplayName("Should update only provided fields")
    void shouldUpdateProfile_whenPartialFieldsProvided() {
        // Given
        String userId = Ulid.fast().toString();
        UpdateUserProfileCommand command =
                new UpdateUserProfileCommand(userId, "NewFirstName", null, null, null, null, null);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setEmail("user@std.iyte.edu.tr");
        userEntity.setFirstName("OldFirstName");
        userEntity.setLastName("OldLastName");
        userEntity.setDescription("Old description");

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
                        null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userDtoMapper.toDto(userEntity)).thenReturn(expectedDto);

        // When
        ApiResponse<UserDto> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("NewFirstName", userEntity.getFirstName());
        assertEquals("OldLastName", userEntity.getLastName());
        assertEquals("Old description", userEntity.getDescription());
        verify(userRepository).save(userEntity);
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
        UpdateUserProfileCommand command =
                new UpdateUserProfileCommand(userId, null, null, null, "   ", "   ", null);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setLinkedinUrl("https://www.linkedin.com/in/old");
        userEntity.setGithubUrl("https://github.com/old");

        UserDto expectedDto = new UserDto(userId, null, null, null, null, null, null, null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userDtoMapper.toDto(userEntity)).thenReturn(expectedDto);

        // When
        ApiResponse<UserDto> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertNull(userEntity.getLinkedinUrl());
        assertNull(userEntity.getGithubUrl());
        verify(userRepository).save(userEntity);
    }

    @Test
    @DisplayName("Should accept valid LinkedIn URL with www")
    void shouldAcceptValidLinkedInUrl_withWww() {
        // Given
        String userId = Ulid.fast().toString();
        UpdateUserProfileCommand command =
                new UpdateUserProfileCommand(
                        userId,
                        null,
                        null,
                        null,
                        "https://www.linkedin.com/in/johndoe",
                        null,
                        null);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);

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

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userDtoMapper.toDto(userEntity)).thenReturn(expectedDto);

        // When
        ApiResponse<UserDto> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("https://www.linkedin.com/in/johndoe", userEntity.getLinkedinUrl());
    }

    @Test
    @DisplayName("Should accept valid LinkedIn URL without www")
    void shouldAcceptValidLinkedInUrl_withoutWww() {
        // Given
        String userId = Ulid.fast().toString();
        UpdateUserProfileCommand command =
                new UpdateUserProfileCommand(
                        userId, null, null, null, "https://linkedin.com/in/johndoe", null, null);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);

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

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userDtoMapper.toDto(userEntity)).thenReturn(expectedDto);

        // When
        ApiResponse<UserDto> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("https://linkedin.com/in/johndoe", userEntity.getLinkedinUrl());
    }

    @Test
    @DisplayName("Should accept valid GitHub URL with path")
    void shouldAcceptValidGithubUrl_withPath() {
        // Given
        String userId = Ulid.fast().toString();
        UpdateUserProfileCommand command =
                new UpdateUserProfileCommand(
                        userId, null, null, null, null, "https://github.com/johndoe/my-repo", null);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);

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

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userDtoMapper.toDto(userEntity)).thenReturn(expectedDto);

        // When
        ApiResponse<UserDto> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("https://github.com/johndoe/my-repo", userEntity.getGithubUrl());
    }
}
