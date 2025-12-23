package com.iyte_yazilim.proje_pazari.domain.entities;

import java.util.ArrayList;
import java.util.List;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("unused")
public class Project extends BaseEntity<Ulid> {

    private String title;
    private String description;
    private String summary;
    private ProjectStatus status = ProjectStatus.DRAFT; // Default state
    private User owner;
    private List<ProjectApplication> applications = new ArrayList<>();

    public void setOwner(User owner) {
        this.owner = owner;
    }

}
