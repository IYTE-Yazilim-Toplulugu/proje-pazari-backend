package com.iyte_yazilim.proje_pazari.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Project statistics for admin analytics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectStatsDTO {

    @Schema(description = "Total number of projects")
    private long totalProjects;

    @Schema(description = "Projects by status distribution")
    private Map<String, Long> projectsByStatus;

    @Schema(description = "Projects by category distribution")
    private Map<String, Long> projectsByCategory;

    @Schema(description = "Average applications per project")
    private double avgApplicationsPerProject;
}
