package com.iyte_yazilim.proje_pazari.application.commands.reviewFlaggedContent;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FlaggedContentNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.FlaggedContentRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.UserMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.FlaggedContentEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewFlaggedContentHandlerTest {

    @Mock private FlaggedContentRepository flaggedContentRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks private ReviewFlaggedContentHandler handler;

    private String flagId;
    private FlaggedContentEntity flagEntity;

    @BeforeEach
    void setUp() {
        flagId = Ulid.fast().toString();
        flagEntity = new FlaggedContentEntity();
        flagEntity.setId(flagId);
        flagEntity.setContentType("PROJECT");
        flagEntity.setContentId(Ulid.fast().toString());
        flagEntity.setStatus("PENDING");
    }

    @Test
    @DisplayName("Should approve flagged content successfully")
    void shouldApproveFlaggedContent_whenFlagExists() {
        when(flaggedContentRepository.findById(flagId)).thenReturn(Optional.of(flagEntity));

        ApiResponse<Void> response =
                handler.handle(new ReviewFlaggedContentCommand(flagId, "APPROVE", "Looks fine"));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("APPROVED", flagEntity.getStatus());
        verify(flaggedContentRepository).save(flagEntity);
    }

    @Test
    @DisplayName("Should remove flagged content successfully")
    void shouldRemoveFlaggedContent_whenFlagExists() {
        when(flaggedContentRepository.findById(flagId)).thenReturn(Optional.of(flagEntity));

        ApiResponse<Void> response =
                handler.handle(new ReviewFlaggedContentCommand(flagId, "REMOVE", "Violates rules"));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("REMOVED", flagEntity.getStatus());
        verify(flaggedContentRepository).save(flagEntity);
    }

    @Test
    @DisplayName("Should ban user and remove flag when content type is USER")
    void shouldBanUser_whenContentTypeIsUser() {
        String userId = Ulid.fast().toString();
        flagEntity.setContentType("USER");
        flagEntity.setContentId(userId);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setIsActive(true);

        User domainUser = new User("user@test.com", "pw", "Test", "User");

        when(flaggedContentRepository.findById(flagId)).thenReturn(Optional.of(flagEntity));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userMapper.entityToDomain(userEntity)).thenReturn(domainUser);

        ApiResponse<Void> response =
                handler.handle(
                        new ReviewFlaggedContentCommand(flagId, "BAN_USER", "Abusive behavior"));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("REMOVED", flagEntity.getStatus());
        assertFalse(domainUser.isActive());
        verify(userMapper).applyDomainToEntity(domainUser, userEntity);
        verify(userRepository).save(userEntity);
        verify(flaggedContentRepository).save(flagEntity);
    }

    @Test
    @DisplayName("Should throw FlaggedContentNotFoundException when flag does not exist")
    void shouldThrowException_whenFlagNotFound() {
        String unknownFlagId = Ulid.fast().toString();
        when(flaggedContentRepository.findById(unknownFlagId)).thenReturn(Optional.empty());

        assertThrows(
                FlaggedContentNotFoundException.class,
                () ->
                        handler.handle(
                                new ReviewFlaggedContentCommand(unknownFlagId, "APPROVE", null)));
        verify(flaggedContentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when BAN_USER targets a non-existent user")
    void shouldThrowException_whenBannedUserNotFound() {
        String unknownUserId = Ulid.fast().toString();
        flagEntity.setContentType("USER");
        flagEntity.setContentId(unknownUserId);

        when(flaggedContentRepository.findById(flagId)).thenReturn(Optional.of(flagEntity));
        when(userRepository.findById(unknownUserId)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> handler.handle(new ReviewFlaggedContentCommand(flagId, "BAN_USER", null)));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return validation error for unknown action")
    void shouldReturnValidationError_whenActionIsInvalid() {
        when(flaggedContentRepository.findById(flagId)).thenReturn(Optional.of(flagEntity));

        ApiResponse<Void> response =
                handler.handle(new ReviewFlaggedContentCommand(flagId, "UNKNOWN_ACTION", null));

        assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
        verify(flaggedContentRepository, never()).save(any());
    }
}
