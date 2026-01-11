package com.iyte_yazilim.proje_pazari.infrastructure.persistence.elasticsearch.services;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectSearchRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectDocument;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Getter
@Setter
@ConditionalOnProperty(
        name = "spring.data.elasticsearch.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class ProjectSearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ProjectSearchRepository projectSearchRepository;

    public List<ProjectDocument> searchProjects(String keyword) {
        // Multi-field search
        Query query =
                NativeQuery.builder()
                        .withQuery(
                                q ->
                                        q.multiMatch(
                                                m ->
                                                        m.query(keyword)
                                                                .fields(
                                                                        "title^3",
                                                                        "description^2",
                                                                        "summary") // Boosting
                                                                .fuzziness("AUTO")
                                                                .operator(Operator.Or)))
                        .build();

        SearchHits<ProjectDocument> searchHits =
                elasticsearchOperations.search(query, ProjectDocument.class);

        return searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    // Changed from Page to SearchPage
    public SearchPage<ProjectDocument> advancedSearch(
            String keyword, String status, List<String> tags, Pageable pageable) {
        CriteriaQuery query = new CriteriaQuery(new Criteria());

        if (keyword != null && !keyword.isBlank()) {
            query.addCriteria(
                    new Criteria("title").contains(keyword).or("description").contains(keyword));
        }

        if (status != null) {
            query.addCriteria(new Criteria("status").is(status));
        }

        if (tags != null && !tags.isEmpty()) {
            query.addCriteria(new Criteria("tags").in(tags));
        }

        query.setPageable(pageable);

        SearchHits<ProjectDocument> searchHits =
                elasticsearchOperations.search(query, ProjectDocument.class);

        return SearchHitSupport.searchPageFor(searchHits, pageable); // Returns SearchPage
    }

    public List<String> getSuggestions(String prefix) {
        Query query =
                NativeQuery.builder()
                        .withQuery(
                                q ->
                                        q.bool(
                                                b ->
                                                        b.should(
                                                                        s ->
                                                                                s.matchPhrasePrefix(
                                                                                        m ->
                                                                                                m.field(
                                                                                                                "title")
                                                                                                        .query(
                                                                                                                prefix)))
                                                                .should(
                                                                        s ->
                                                                                s.matchPhrasePrefix(
                                                                                        m ->
                                                                                                m.field(
                                                                                                                "summary")
                                                                                                        .query(
                                                                                                                prefix)))))
                        .withMaxResults(10)
                        .build();

        SearchHits<ProjectDocument> searchHits =
                elasticsearchOperations.search(query, ProjectDocument.class);

        return searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent().getTitle())
                .distinct()
                .collect(Collectors.toList());
    }

    public Map<String, Long> getProjectStatistics() {
        // Aggregation example
        Query query =
                NativeQuery.builder()
                        .withAggregation(
                                "status_count",
                                Aggregation.of(a -> a.terms(t -> t.field("status"))))
                        .build();

        SearchHits<ProjectDocument> searchHits =
                elasticsearchOperations.search(query, ProjectDocument.class);

        // Process aggregations
        return extractAggregationResults(searchHits);
    }

    private Map<String, Long> extractAggregationResults(SearchHits<ProjectDocument> searchHits) {
        Map<String, Long> result = new HashMap<>();

        AggregationsContainer<?> aggregations = searchHits.getAggregations();

        if (aggregations instanceof ElasticsearchAggregations esAggregations) {
            // 1. Safely retrieve the wrapper for the specific aggregation
            var statusAggWrapper = esAggregations.get("status_count");

            // 2. Check if the wrapper exists and has an inner aggregation
            if (statusAggWrapper != null) {

                Aggregate statusAggregate = statusAggWrapper.aggregation().getAggregate();

                // 3. Now it is safe to check the type
                if (statusAggregate.isSterms()) {
                    for (StringTermsBucket bucket : statusAggregate.sterms().buckets().array()) {
                        result.put(bucket.key().stringValue(), bucket.docCount());
                    }
                }
            }
        }
        return result;
    }
}
