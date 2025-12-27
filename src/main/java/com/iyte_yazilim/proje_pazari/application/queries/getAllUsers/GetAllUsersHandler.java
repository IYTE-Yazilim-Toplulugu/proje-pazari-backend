package com.iyte_yazilim.proje_pazari.application.queries.getAllUsers;

import com.iyte_yazilim.proje_pazari.application.dtos.UserDto;
import com.iyte_yazilim.proje_pazari.application.mappers.UserDtoMapper;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.UserMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetAllUsersHandler
        implements IRequestHandler<GetAllUsersQuery, ApiResponse<List<UserDto>>> {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserDtoMapper userDtoMapper;

    @Override
    public ApiResponse<List<UserDto>> handle(GetAllUsersQuery query) {

        // --- 1. Find all users ---
        List<UserEntity> userEntities = userRepository.findAll();

        // --- 2. Map to domain and then to DTO ---
        List<UserDto> users =
                userEntities.stream()
                        .map(userMapper::entityToDomain)
                        .map(userDtoMapper::domainToDto)
                        .toList();

        // --- 3. Response ---
        return ApiResponse.success(users, "Users retrieved successfully");
    }
}
