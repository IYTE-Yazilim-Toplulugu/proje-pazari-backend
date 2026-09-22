package com.iyte_yazilim.proje_pazari.presentation.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iyte_yazilim.proje_pazari.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OpenAPI - user ID field is standardized as userId")
class OpenApiUserIdSchemaTest extends IntegrationTestBase {

    private static final String SCHEMAS = "$.components.schemas";

    @Test
    @DisplayName("UserProfileDTO exposes userId and no longer exposes id")
    void userProfileDtoExposesUserId() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(SCHEMAS + ".UserProfileDTO.properties.userId").exists())
                .andExpect(jsonPath(SCHEMAS + ".UserProfileDTO.properties.id").doesNotExist());
    }

    @Test
    @DisplayName("Other user DTOs continue to use userId")
    void otherUserDtosUseUserId() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(SCHEMAS + ".UserDto.properties.userId").exists())
                .andExpect(jsonPath(SCHEMAS + ".UserDto.properties.id").doesNotExist())
                .andExpect(jsonPath(SCHEMAS + ".UserAdminDTO.properties.userId").exists())
                .andExpect(jsonPath(SCHEMAS + ".UserAdminDTO.properties.id").doesNotExist());
    }
}
