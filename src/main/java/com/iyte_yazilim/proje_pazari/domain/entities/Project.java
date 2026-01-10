package com.iyte_yazilim.proje_pazari.domain.entities;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import java.time.LocalDateTime;
import java.util.List;
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
    private ProjectStatus status = ProjectStatus.DRAFT;
    private User owner;
    private List<ProjectApplication> applications;

    private Integer maxTeamSize;
    private Integer currentTeamSize;
    private List<String> requiredSkills;
    private String category;
    private LocalDateTime deadline;

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
