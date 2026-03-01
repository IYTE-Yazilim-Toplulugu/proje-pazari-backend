package com.iyte_yazilim.proje_pazari.application.queries.getAllUsers;

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
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAllUsersHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserDtoMapper userDtoMapper;

    @InjectMocks
    private GetAllUsersHandler handler;

    @Test
    @DisplayName("Should return all users successfully")
    void shouldReturnAllUsers_whenUsersExist() {
        // Given
        GetAllUsersQuery query = new GetAllUsersQuery();

        UserEntity entity1 = new UserEntity();
        entity1.setId("user-1");
        entity1.setEmail("user1@std.iyte.edu.tr");

        UserEntity entity2 = new UserEntity();
        entity2.setId("user-2");
        entity2.setEmail("user2@std.iyte.edu.tr");

        User domain1 = new User();
        domain1.setId(Ulid.fast());
        domain1.setEmail("user1@std.iyte.edu.tr");

        User domain2 = new User();
        domain2.setId(Ulid.fast());
        domain2.setEmail("user2@std.iyte.edu.tr");

        UserDto dto1 = new UserDto(
                "user-1",
                "user1@std.iyte.edu.tr",
                "John",
                "Doe",
                null,
                null,
                null,
                null,
                null);
        UserDto dto2 = new UserDto(
                "user-2",
                "user2@std.iyte.edu.tr",
                "Jane",
                "Smith",
                null,
                null,
                null,
                null,
                null);

        when(userRepository.findAll()).thenReturn(List.of(entity1, entity2));
        when(userMapper.entityToDomain(entity1)).thenReturn(domain1);
        when(userMapper.entityToDomain(entity2)).thenReturn(domain2);
        when(userDtoMapper.domainToDto(domain1)).thenReturn(dto1);
        when(userDtoMapper.domainToDto(domain2)).thenReturn(dto2);

        // When
        ApiResponse<List<UserDto>> response = handler.handle(query);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("Users retrieved successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(2, response.getData().size());
        assertEquals("user1@std.iyte.edu.tr", response.getData().get(0).email());
        assertEquals("user2@std.iyte.edu.tr", response.getData().get(1).email());
    }

    @Test
    @DisplayName("Should return empty list when no users exist")
    void shouldReturnEmptyList_whenNoUsersExist() {
        // Given
        GetAllUsersQuery query = new GetAllUsersQuery();

        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        ApiResponse<List<UserDto>> response = handler.handle(query);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertNotNull(response.getData());
        assertTrue(response.getData().isEmpty());
    }
}
