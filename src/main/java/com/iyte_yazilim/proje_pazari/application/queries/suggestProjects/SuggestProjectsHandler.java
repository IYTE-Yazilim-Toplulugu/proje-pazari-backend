package com.iyte_yazilim.proje_pazari.application.queries.suggestProjects;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.services.ProjectSearchService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "spring.data.elasticsearch.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class SuggestProjectsHandler
        implements IRequestHandler<SuggestProjectsQuery, ApiResponse<List<String>>> {

    private final ProjectSearchService searchService;

    @Override
    public ApiResponse<List<String>> handle(SuggestProjectsQuery query) {
        List<String> suggestions = searchService.getSuggestions(query.q());
        return ApiResponse.success(suggestions, "Suggestions retrieved successfully");
    }
}
