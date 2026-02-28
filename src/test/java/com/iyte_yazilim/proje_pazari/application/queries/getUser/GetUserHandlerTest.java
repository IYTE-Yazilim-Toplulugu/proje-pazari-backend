package com.iyte_yazilim.proje_pazari.application.queries.getUser;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.dtos.UserDto;
import com.iyte_yazilim.proje_pazari.application.mappers.UserDtoMapper;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.UserMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetUserHandlerTest {

    @Mock private UserRepository userRepository;

    @Mock private UserMapper userMapper;

    @Mock private UserDtoMapper userDtoMapper;

    @InjectMocks private GetUserHandler handler;

    @Test
    @DisplayName("Should return user successfully when user exists")
    void shouldReturnUser_whenUserExists() {
        // Given
        String userId = Ulid.fast().toString();
        GetUserQuery query = new GetUserQuery(userId);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setEmail("test@std.iyte.edu.tr");
        userEntity.setFirstName("John");
        userEntity.setLastName("Doe");

        User domainUser = new User();
        domainUser.setId(Ulid.fast());
        domainUser.setEmail("test@std.iyte.edu.tr");

        UserDto expectedDto =
                new UserDto(userId, "test@std.iyte.edu.tr", "John", "Doe", null, null, null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userMapper.entityToDomain(userEntity)).thenReturn(domainUser);
        when(userDtoMapper.domainToDto(domainUser)).thenReturn(expectedDto);

        // When
        ApiResponse<UserDto> response = handler.handle(query);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("User retrieved successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(userId, response.getData().userId());
        assertEquals("test@std.iyte.edu.tr", response.getData().email());
    }

    @Test
    @DisplayName("Should return not found when user does not exist")
    void shouldReturnNotFound_whenUserDoesNotExist() {
        // Given
        String userId = Ulid.fast().toString();
        GetUserQuery query = new GetUserQuery(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        ApiResponse<UserDto> response = handler.handle(query);

        // Then
        assertEquals(ResponseCode.NOT_FOUND, response.getCode());
        assertEquals("User not found", response.getMessage());
        assertNull(response.getData());
        verify(userMapper, never()).entityToDomain(any());
    }
}
