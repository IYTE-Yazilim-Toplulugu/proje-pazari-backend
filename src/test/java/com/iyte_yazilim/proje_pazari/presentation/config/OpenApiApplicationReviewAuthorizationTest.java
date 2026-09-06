package com.iyte_yazilim.proje_pazari.presentation.config;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iyte_yazilim.proje_pazari.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OpenApiApplicationReviewAuthorizationTest extends IntegrationTestBase {

    private static final String FORBIDDEN_RESPONSE_PATH =
            "$.paths['/api/v1/applications/{applicationId}/review'].put.responses['403']";
    private static final String REQUEST_SCHEMA_PATH =
            "$.components.schemas.ReviewApplicationRequest";

    @Test
    @DisplayName("Application review documents project-owner authorization failures")
    void documentsProjectOwnerAuthorizationFailure() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(FORBIDDEN_RESPONSE_PATH + ".description")
                                .value(containsString("does not own")))
                .andExpect(jsonPath(REQUEST_SCHEMA_PATH + ".properties.status").exists())
                .andExpect(jsonPath(REQUEST_SCHEMA_PATH + ".properties.reviewMessage").exists())
                .andExpect(jsonPath(REQUEST_SCHEMA_PATH + ".properties.requesterId").doesNotExist())
                .andExpect(
                        jsonPath(REQUEST_SCHEMA_PATH + ".properties.requesterRole").doesNotExist());
    }
}
