package com.iyte_yazilim.proje_pazari.presentation.config;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iyte_yazilim.proje_pazari.IntegrationTestBase;
import org.junit.jupiter.api.Test;

class OpenApiBulkApplicationReviewTest extends IntegrationTestBase {

    private static final String DESCRIPTION =
            "$.paths['/api/v1/admin/applications/bulk-action'].post.description";

    @Test
    void documentsIndependentTransactionsAndPartialSuccess() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(DESCRIPTION).value(containsString("independent transaction")))
                .andExpect(jsonPath(DESCRIPTION).value(containsString("partial success")))
                .andExpect(jsonPath(DESCRIPTION).value(containsString("PROJECT_FULL")));
    }
}
