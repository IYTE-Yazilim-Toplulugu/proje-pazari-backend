package com.iyte_yazilim.proje_pazari.application.queries.getCurrentUserProfile;

import com.iyte_yazilim.proje_pazari.application.dtos.UserProfileDTO;
import com.iyte_yazilim.proje_pazari.application.queries.getUserProfile.GetUserProfileQuery;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetCurrentUserProfileHandler implements IRequestHandler<GetCurrentUserProfileQuery, ApiResponse<UserProfileDTO>> {

    private final IRequestHandler<GetUserProfileQuery, ApiResponse<UserProfileDTO>> getUserProfileHandler;

    @Override
    public ApiResponse<UserProfileDTO> handle(GetCurrentUserProfileQuery query) {
        // Reuse GetUserProfileHandler
        return getUserProfileHandler.handle(new GetUserProfileQuery(query.authenticatedUserId()));
    }
}
