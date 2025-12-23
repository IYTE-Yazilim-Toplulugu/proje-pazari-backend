package com.example.demo.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Project
 */
@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 5000)
    private String description;

    @Column(length = 500)
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.DRAFT; // Default state

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProjectApplication> applications = new ArrayList<>();

    public boolean addApplication(ProjectApplication projectApplication) {
        if (projectApplication == null)
            return false;
        projectApplication.setProject(this);
        return applications.add(projectApplication);
    }

    public boolean isOpenForApplications() {
        return this.status == ProjectStatus.OPEN;
    }
}
