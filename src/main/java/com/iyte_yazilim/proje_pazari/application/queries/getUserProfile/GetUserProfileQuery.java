package com.iyte_yazilim.proje_pazari.application.queries.getUserProfile;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.UserProfileDTO;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Query to get user profile by user ID")
public record GetUserProfileQuery(
        @Schema(
                        description = "User ID to retrieve profile for",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                String userId)
        implements IRequest<ApiResponse<UserProfileDTO>> {}
