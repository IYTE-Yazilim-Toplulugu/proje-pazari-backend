package com.iyte_yazilim.proje_pazari.application.queries.getUser;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.dtos.UserDto;
import com.iyte_yazilim.proje_pazari.application.mappers.UserDtoMapper;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.UserMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetUserHandler implements IRequestHandler<GetUserQuery, ApiResponse<UserDto>> {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserDtoMapper userDtoMapper;
    private final MessageService messageService;

    @Override
    public ApiResponse<UserDto> handle(GetUserQuery query) {

        // --- 1. Find user by ID ---
        UserEntity userEntity = userRepository.findById(query.userId()).orElse(null);
        if (userEntity == null) {
            return ApiResponse.notFound(messageService.getMessage("user.not.found"));
        }

        // --- 2. Map to domain ---
        User user = userMapper.entityToDomain(userEntity);

        // --- 3. Map to DTO ---
        UserDto userDto = userDtoMapper.domainToDto(user);

        // --- 4. Response ---
        return ApiResponse.success(userDto, messageService.getMessage("user.retrieved.success"));
    }
}
