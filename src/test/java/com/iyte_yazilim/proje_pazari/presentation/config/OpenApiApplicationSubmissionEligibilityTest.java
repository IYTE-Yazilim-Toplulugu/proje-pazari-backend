package com.iyte_yazilim.proje_pazari.presentation.config;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iyte_yazilim.proje_pazari.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OpenApiApplicationSubmissionEligibilityTest extends IntegrationTestBase {

    private static final String RESPONSES =
            "$.paths['/api/v1/projects/{projectId}/applications'].post.responses";

    @Test
    @DisplayName("Application submission documents all eligibility response categories")
    void documentsSubmissionEligibilityFailures() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(RESPONSES + "['403'].description")
                                .value(containsString("SELF_APPLICATION_NOT_ALLOWED")))
                .andExpect(
                        jsonPath(RESPONSES + "['409'].description")
                                .value(containsString("APPLICATION_ALREADY_EXISTS")))
                .andExpect(
                        jsonPath(RESPONSES + "['409'].description")
                                .value(containsString("PROJECT_NOT_OPEN")))
                .andExpect(
                        jsonPath(RESPONSES + "['409'].description")
                                .value(containsString("PROJECT_APPLICATION_DEADLINE_PASSED")))
                .andExpect(
                        jsonPath(RESPONSES + "['409'].description")
                                .value(containsString("PROJECT_FULL")));
    }
}
