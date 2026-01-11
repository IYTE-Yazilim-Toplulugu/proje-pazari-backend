package com.iyte_yazilim.proje_pazari.infrastructure.persistence.models;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Document(indexName = "#{@environment.getProperty('app.elasticsearch.project-index')}")
@Setting(settingPath = "elasticsearch/project-settings.json")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDocument {

    @Id private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Text)
    private String summary;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Nested)
    private OwnerInfo owner;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private LocalDateTime updatedAt;

    @Field(type = FieldType.Integer)
    private int applicationsCount;

    @Data
    public static class OwnerInfo {
        @Field(type = FieldType.Keyword)
        private String id;

        @Field(type = FieldType.Text)
        private String name;

        @Field(type = FieldType.Keyword)
        private String email;
    }
}
