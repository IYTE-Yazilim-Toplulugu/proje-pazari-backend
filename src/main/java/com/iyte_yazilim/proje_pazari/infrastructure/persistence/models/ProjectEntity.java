package com.iyte_yazilim.proje_pazari.infrastructure.persistence.models;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEntity {

    @Id
    @Column(length = 26)
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectApplicationEntity> applications;

    @Column(name = "max_team_size")
    private Integer maxTeamSize;

    @Column(name = "current_team_size")
    private Integer currentTeamSize;

    @ElementCollection
    @Column(name = "required_skill")
    private List<String> requiredSkills;

    @Column(name = "category")
    private String category;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null || id.isBlank()) {
            id = Ulid.fast().toString();
        }
        if (status == null) {
            status = ProjectStatus.DRAFT;
        }
        if (currentTeamSize == null) {
            currentTeamSize = 1; // Owner is automatically part of the team
        }
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}