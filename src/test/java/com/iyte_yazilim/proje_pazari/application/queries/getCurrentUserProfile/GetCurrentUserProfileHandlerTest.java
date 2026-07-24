package com.iyte_yazilim.proje_pazari.application.queries.getCurrentUserProfile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.common.ResponseCode;
import com.iyte_yazilim.proje_pazari.application.dtos.UserProfileDTO;
import com.iyte_yazilim.proje_pazari.application.queries.getUserProfile.GetUserProfileQuery;
import java.time.LocalDateTime;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetCurrentUserProfileHandlerTest {

    @Mock
    private IRequestHandler<GetUserProfileQuery, ApiResponse<UserProfileDTO>> getUserProfileHandler;

    @InjectMocks private GetCurrentUserProfileHandler handler;

    @Test
    @DisplayName("Should delegate to GetUserProfileHandler with correct user ID")
    void shouldDelegateToGetUserProfileHandler() {
        // Given
        String authenticatedUserId = "auth-user-123";
        GetCurrentUserProfileQuery query = new GetCurrentUserProfileQuery(authenticatedUserId);

        UserProfileDTO expectedProfile =
                new UserProfileDTO(
                        authenticatedUserId,
                        "test@std.iyte.edu.tr",
                        "John",
                        "Doe",
                        "John Doe",
                        null,
                        null,
                        null,
                        null,
                        null,
                        LocalDateTime.now(),
                        0,
                        0,
                        Collections.emptyList(),
                        "USER");

        ApiResponse<UserProfileDTO> expectedResponse =
                ApiResponse.success(expectedProfile, "User profile retrieved successfully");

        when(getUserProfileHandler.handle(any(GetUserProfileQuery.class)))
                .thenReturn(expectedResponse);

        // When
        ApiResponse<UserProfileDTO> response = handler.handle(query);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertNotNull(response.getData());
        assertEquals(authenticatedUserId, response.getData().id());

        // Verify delegation with correct user ID
        ArgumentCaptor<GetUserProfileQuery> captor =
                ArgumentCaptor.forClass(GetUserProfileQuery.class);
        verify(getUserProfileHandler).handle(captor.capture());
        assertEquals(authenticatedUserId, captor.getValue().userId());
    }

    @Test
    @DisplayName("Should propagate not found response from delegate")
    void shouldPropagateNotFound_fromDelegate() {
        // Given
        String userId = "nonexistent-user";
        GetCurrentUserProfileQuery query = new GetCurrentUserProfileQuery(userId);

        ApiResponse<UserProfileDTO> notFoundResponse = ApiResponse.notFound("User not found");

        when(getUserProfileHandler.handle(any(GetUserProfileQuery.class)))
                .thenReturn(notFoundResponse);

        // When
        ApiResponse<UserProfileDTO> response = handler.handle(query);

        // Then
        assertEquals(ResponseCode.NOT_FOUND, response.getCode());
        assertNull(response.getData());
    }
}
