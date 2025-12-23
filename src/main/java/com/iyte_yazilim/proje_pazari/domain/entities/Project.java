package com.iyte_yazilim.proje_pazari.domain.entities;

import java.util.ArrayList;
import java.util.List;

import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@SuppressWarnings("unused")
public class Project extends BaseEntity {

    private String title;
    private String description;
    private String summary;
    private ProjectStatus status = ProjectStatus.DRAFT; // Default state
    private User owner;
    private List<ProjectApplication> applications = new ArrayList<>();

}
