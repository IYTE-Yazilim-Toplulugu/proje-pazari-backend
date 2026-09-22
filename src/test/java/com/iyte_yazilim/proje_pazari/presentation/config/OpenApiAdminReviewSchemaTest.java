package com.iyte_yazilim.proje_pazari.presentation.config;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iyte_yazilim.proje_pazari.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OpenApiAdminReviewSchemaTest extends IntegrationTestBase {

    private static final String REQUEST_SCHEMA_PATH =
            "$.components.schemas.AdminReviewApplicationRequest";
    private static final String RESPONSE_SCHEMA_PATH =
            "$.paths['/api/v1/admin/applications/{applicationId}/review'].put.responses['200']"
                    + ".content['*/*'].schema";

    @Test
    @DisplayName("Admin review documents the shared request and result contract")
    void documentsSharedAdminReviewContract() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(REQUEST_SCHEMA_PATH + ".properties.status").exists())
                .andExpect(jsonPath(REQUEST_SCHEMA_PATH + ".properties.reviewMessage").exists())
                .andExpect(jsonPath(REQUEST_SCHEMA_PATH + ".required").value(hasItem("status")))
                .andExpect(
                        jsonPath(RESPONSE_SCHEMA_PATH + "['$ref']")
                                .value(containsString("ReviewApplicationCommandResult")));
    }
}
