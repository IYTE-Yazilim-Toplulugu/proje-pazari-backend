package com.iyte_yazilim.proje_pazari.infrastructure.persistence.models;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Document(indexName = "#{@environment.getProperty('app.elasticsearch.project-index', 'projects')}")
@Setting(settingPath = "elasticsearch/project-settings.json")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDocument {

    @Id private String id;

    @Field(type = FieldType.Text, analyzer = "turkish_search")
    private String title;

    @Field(type = FieldType.Text, analyzer = "turkish_search")
    private String description;

    @Field(type = FieldType.Text, analyzer = "turkish_search")
    private String summary;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private String ownerId;

    @Field(type = FieldType.Text)
    private String ownerName;

    @Field(type = FieldType.Keyword)
    private String ownerEmail;

    @Field(type = FieldType.Integer)
    private Integer maxTeamSize;

    @Field(type = FieldType.Keyword)
    private List<String> requiredSkills;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_fraction)
    private LocalDateTime deadline;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_fraction)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_fraction)
    private LocalDateTime updatedAt;

    @Field(type = FieldType.Integer)
    private int applicationCount;
}
