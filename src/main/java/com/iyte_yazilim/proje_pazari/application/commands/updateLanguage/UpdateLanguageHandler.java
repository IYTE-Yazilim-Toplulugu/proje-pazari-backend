package com.iyte_yazilim.proje_pazari.application.commands.updateLanguage;

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
 * Handler for updating a user's preferred language.
 *
 * <p>This handler processes the {@link UpdateLanguageCommand} and updates
 * the user's language preference in the database.
 *
 * <p><strong>Features:</strong>
 * <ul>
 *   <li>Validates user existence</li>
 *   <li>Updates preferredLanguage field</li>
 *   <li>Returns localized success message in the NEW language</li>
 *   <li>Transactional operation</li>
 * </ul>
 *
 * <p><strong>Response Message Language:</strong>
 * The success message is returned in the user's NEW preferred language.
 * For example, if changing from Turkish to English, the success message
 * will be in English.
 *
 * @see UpdateLanguageCommand
 * @see UserEntity
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateLanguageHandler
        implements IRequestHandler<UpdateLanguageCommand, ApiResponse<Void>> {

    private final UserRepository userRepository;
    private final MessageService messageService;

    /**
     * Handles the language update command.
     *
     * <p><strong>Process:</strong>
     * <ol>
     *   <li>Validate the command</li>
     *   <li>Find the user by ID</li>
     *   <li>Update the preferredLanguage field</li>
     *   <li>Save the updated user</li>
     *   <li>Return success message in the NEW language</li>
     * </ol>
     *
     * <p><strong>Error Cases:</strong>
     * <ul>
     *   <li>Invalid command → Validation error (400)</li>
     *   <li>User not found → Not found error (404)</li>
     * </ul>
     *
     * @param command the language update command
     * @return ApiResponse with success message in the new language
     */
    @Override
    @Transactional
    public ApiResponse<Void> handle(UpdateLanguageCommand command) {
        // --- 1. Validation ---
        try {
            command.validate();
        } catch (IllegalArgumentException e) {
            log.warn("Language update validation failed: {}", e.getMessage());
            return ApiResponse.validationError(e.getMessage());
        }

        // --- 2. Find User ---
        UserEntity user = userRepository.findById(command.userId()).orElse(null);

        if (user == null) {
            log.warn("User not found for language update: {}", command.userId());
            return ApiResponse.notFound(messageService.getMessage("user.not.found"));
        }

        // --- 3. Store Old Language for Logging ---
        String oldLanguage = user.getPreferredLanguage();
        String newLanguage = command.getNormalizedLanguage();

        // --- 4. Update Language ---
        user.setPreferredLanguage(newLanguage);

        // --- 5. Save User ---
        userRepository.save(user);

        log.info(
                "User language updated: userId={}, oldLanguage={}, newLanguage={}",
                command.userId(),
                oldLanguage,
                newLanguage);

        // --- 6. Return Success Message in NEW Language ---
        // Important: Get the message in the user's NEW preferred language
        // so they see the confirmation in their chosen language
        java.util.Locale newLocale = java.util.Locale.forLanguageTag(newLanguage);
        String successMessage =
                messageService.getMessage("user.language.updated", null, newLocale);

        return ApiResponse.success(null, successMessage);
    }
}