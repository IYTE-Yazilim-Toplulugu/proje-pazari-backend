package com.iyte_yazilim.proje_pazari.presentation.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iyte_yazilim.proje_pazari.IntegrationTestBase;
import org.junit.jupiter.api.Test;

class OpenApiApplicationMessageContractTest extends IntegrationTestBase {

    private static final String MESSAGE_PATH =
            "$.paths['/api/v1/applications/{applicationId}/messages']";
    private static final String REQUEST_SCHEMA =
            "$.components.schemas.SendApplicationMessageRequest.properties";

    @Test
    void documentsSharedParticipantOnlyPlainTextContract() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(MESSAGE_PATH + ".get").exists())
                .andExpect(jsonPath(MESSAGE_PATH + ".get.responses['403']").exists())
                .andExpect(jsonPath(MESSAGE_PATH + ".post").exists())
                .andExpect(jsonPath(MESSAGE_PATH + ".post.responses['201']").exists())
                .andExpect(jsonPath(MESSAGE_PATH + ".post.responses['403']").exists())
                .andExpect(jsonPath(REQUEST_SCHEMA + ".body.maxLength").value(2000))
                .andExpect(jsonPath(REQUEST_SCHEMA + ".senderId").doesNotExist())
                .andExpect(jsonPath(REQUEST_SCHEMA + ".requesterId").doesNotExist());
    }
}
