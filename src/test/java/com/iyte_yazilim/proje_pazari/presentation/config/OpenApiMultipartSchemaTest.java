package com.iyte_yazilim.proje_pazari.presentation.config;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iyte_yazilim.proje_pazari.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OpenApiMultipartSchemaTest extends IntegrationTestBase {

    @Nested
    @DisplayName("POST /api/v1/users/me/profile-picture - OpenAPI schema")
    class ProfilePictureSchemaTests {

        private static final String CONTENT_PATH =
                "$.paths['/api/v1/users/me/profile-picture'].post.requestBody"
                        + ".content['multipart/form-data']";

        @Test
        @DisplayName("declares file as required string/binary, not a generic object")
        void declaresFileAsRequiredBinary() throws Exception {
            mockMvc.perform(get("/v3/api-docs"))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath(CONTENT_PATH + ".schema.properties.file.type").value("string"))
                    .andExpect(
                            jsonPath(CONTENT_PATH + ".schema.properties.file.format")
                                    .value("binary"))
                    .andExpect(jsonPath(CONTENT_PATH + ".schema.required").value(hasItem("file")));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/files - OpenAPI schema")
    class FileUploadSchemaTests {

        private static final String CONTENT_PATH =
                "$.paths['/api/v1/files'].post.requestBody.content['multipart/form-data']";

        @Test
        @DisplayName("declares file as required string/binary, not a generic object")
        void declaresFileAsRequiredBinary() throws Exception {
            mockMvc.perform(get("/v3/api-docs"))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath(CONTENT_PATH + ".schema.properties.file.type").value("string"))
                    .andExpect(
                            jsonPath(CONTENT_PATH + ".schema.properties.file.format")
                                    .value("binary"))
                    .andExpect(jsonPath(CONTENT_PATH + ".schema.required").value(hasItem("file")));
        }
    }
}
