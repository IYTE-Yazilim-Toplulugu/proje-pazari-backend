package com.iyte_yazilim.proje_pazari.application.queries.searchProjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedProjectsResult;
import com.iyte_yazilim.proje_pazari.application.dtos.ProjectDetailDto;
import com.iyte_yazilim.proje_pazari.application.services.ProjectSearchService;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectDocument;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchPage;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchProjectsHandler Tests")
class SearchProjectsHandlerTest {

    @Mock private ProjectSearchService searchService;

    @InjectMocks private SearchProjectsHandler handler;

    private SearchProjectsQuery query() {
        return new SearchProjectsQuery("test", null, 0, 10);
    }

    @SuppressWarnings("unchecked")
    private void mockSearchReturns(ProjectDocument document) {
        SearchHit<ProjectDocument> hit =
                (SearchHit<ProjectDocument>) org.mockito.Mockito.mock(SearchHit.class);
        when(hit.getContent()).thenReturn(document);

        SearchPage<ProjectDocument> page =
                (SearchPage<ProjectDocument>) org.mockito.Mockito.mock(SearchPage.class);
        when(page.getContent()).thenReturn(List.of(hit));
        when(page.getNumber()).thenReturn(0);
        when(page.getTotalPages()).thenReturn(1);
        when(page.getTotalElements()).thenReturn(1L);

        when(searchService.advancedSearch(eq("test"), any(), any())).thenReturn(page);
    }

    @Test
    @DisplayName("Should map a valid status string to the matching enum")
    void shouldMapValidStatus() {
        mockSearchReturns(ProjectDocument.builder().id("1").status("OPEN").build());

        ApiResponse<PagedProjectsResult> response = handler.handle(query());

        ProjectDetailDto dto = response.getData().projects().get(0);
        assertThat(dto.status()).isEqualTo(ProjectStatus.OPEN);
    }

    @Test
    @DisplayName("Should degrade an unknown status to null without failing the search")
    void shouldDegradeUnknownStatusToNull() {
        mockSearchReturns(ProjectDocument.builder().id("1").status("INVALID_STATUS").build());

        ApiResponse<PagedProjectsResult> response = handler.handle(query());

        ProjectDetailDto dto = response.getData().projects().get(0);
        assertThat(dto.status()).isNull();
    }

    @Test
    @DisplayName("Should map a null status to null without throwing")
    void shouldMapNullStatusToNull() {
        ProjectDocument document = ProjectDocument.builder().id("1").status(null).build();

        assertThatCode(
                        () -> {
                            mockSearchReturns(document);
                            ProjectDetailDto dto =
                                    handler.handle(query()).getData().projects().get(0);
                            assertThat(dto.status()).isNull();
                        })
                .doesNotThrowAnyException();
    }
}
