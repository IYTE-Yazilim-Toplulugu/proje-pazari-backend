package com.iyte_yazilim.proje_pazari.application.commands.updateUserProfile;

import com.iyte_yazilim.proje_pazari.application.dtos.UserDto;
import com.iyte_yazilim.proje_pazari.application.mappers.UserDtoMapper;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for updating user profile information.
 *
 * <p>This handler processes the {@link UpdateUserProfileCommand} and updates
 * the user's profile fields in the database.
 *
 * <p><strong>Features:</strong>
 * <ul>
 *   <li>Validates command</li>
 *   <li>Updates only non-null fields</li>
 *   <li>Supports updating language preference</li>
 *   <li>Returns localized success message</li>
 *   <li>Transactional operation</li>
 * </ul>
 *
 * @see UpdateUserProfileCommand
 * @see UserEntity
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateUserProfileHandler
        implements IRequestHandler<UpdateUserProfileCommand, ApiResponse<UserDto>> {

    private final UserRepository userRepository;
    private final UserDtoMapper userDtoMapper;
    private final MessageService messageService;

    @Override
    @Transactional
    public ApiResponse<UserDto> handle(UpdateUserProfileCommand command) {
        // --- 1. Validation ---
        try {
            command.validate();
        } catch (IllegalArgumentException e) {
            log.warn("Profile update validation failed: {}", e.getMessage());
            return ApiResponse.validationError(e.getMessage());
        }

        // --- 2. Find User ---
        UserEntity user = userRepository.findById(command.userId()).orElse(null);

        if (user == null) {
            log.warn("User not found for profile update: {}", command.userId());
            return ApiResponse.notFound(
                    messageService.getMessage("user.not.found.with.id", new Object[] {command.userId()}));
        }

        // --- 3. Update Fields (only if not null) ---

        if (command.firstName() != null) {
            user.setFirstName(command.firstName());
            log.debug("Updated firstName for user {}", command.userId());
        }

        if (command.lastName() != null) {
            user.setLastName(command.lastName());
            log.debug("Updated lastName for user {}", command.userId());
        }

        if (command.description() != null) {
            user.setDescription(command.description());
            log.debug("Updated description for user {}", command.userId());
        }

        if (command.linkedinUrl() != null) {
            user.setLinkedinUrl(command.linkedinUrl().isBlank() ? null : command.linkedinUrl());
            log.debug("Updated linkedinUrl for user {}", command.userId());
        }

        if (command.githubUrl() != null) {
            user.setGithubUrl(command.githubUrl().isBlank() ? null : command.githubUrl());
            log.debug("Updated githubUrl for user {}", command.userId());
        }

        // --- 4. Update Preferred Language (if provided) ---
        if (command.preferredLanguage() != null && !command.preferredLanguage().isBlank()) {
            String oldLanguage = user.getPreferredLanguage();
            String newLanguage = command.preferredLanguage().toLowerCase().trim();

            // Validate language
            if (newLanguage.equals("tr") || newLanguage.equals("en")) {
                user.setPreferredLanguage(newLanguage);
                log.info(
                        "Updated preferredLanguage for user {}: {} -> {}",
                        command.userId(),
                        oldLanguage,
                        newLanguage);
            } else {
                log.warn(
                        "Invalid language '{}' provided for user {}. Keeping existing: {}",
                        newLanguage,
                        command.userId(),
                        oldLanguage);
            }
        }

        // --- 5. Save User ---
        UserEntity savedUser = userRepository.save(user);
        log.info("Profile updated successfully for user {}", command.userId());

        // --- 6. Map to DTO ---
        UserDto userDto = userDtoMapper.toDto(savedUser);

        // --- 7. Return Success Response ---
        return ApiResponse.success(userDto, messageService.getMessage("user.profile.updated"));
    }
}