package com.iyte_yazilim.proje_pazari.application.commands.adminDeleteUser;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ResponseCode;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminDeleteUserHandlerTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private AdminDeleteUserHandler handler;

    private String userId;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userId = Ulid.fast().toString();
        userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setEmail("user@std.iyte.edu.tr");
        userEntity.setIsActive(true);
    }

    @Test
    @DisplayName("Should deactivate user successfully")
    void shouldDeactivateUser_whenUserExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        ApiResponse<Void> response = handler.handle(new AdminDeleteUserCommand(userId));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertFalse(userEntity.getIsActive());
        verify(userRepository).save(userEntity);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist")
    void shouldThrowException_whenUserNotFound() {
        String unknownId = Ulid.fast().toString();
        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> handler.handle(new AdminDeleteUserCommand(unknownId)));
        verify(userRepository, never()).save(any());
    }
}
