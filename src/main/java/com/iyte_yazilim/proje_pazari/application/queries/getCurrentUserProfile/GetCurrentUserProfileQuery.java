package com.iyte_yazilim.proje_pazari.application.queries.getCurrentUserProfile;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.UserProfileDTO;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Query to get current authenticated user's profile")
public record GetCurrentUserProfileQuery(
        @Schema(description = "Authenticated user ID", requiredMode = Schema.RequiredMode.REQUIRED)
                String authenticatedUserId)
        implements IRequest<ApiResponse<UserProfileDTO>> {}
