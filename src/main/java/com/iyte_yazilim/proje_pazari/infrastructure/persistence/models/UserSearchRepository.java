package com.iyte_yazilim.proje_pazari.infrastructure.persistence.models;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface UserSearchRepository extends ElasticsearchRepository<UserDocument, String> {

    List<UserDocument> findByFullNameContaining(String name);

    List<UserDocument> findByEmail(String email);
}
