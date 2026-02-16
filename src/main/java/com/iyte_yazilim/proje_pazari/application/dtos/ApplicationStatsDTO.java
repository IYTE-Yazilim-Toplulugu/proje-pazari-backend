package com.iyte_yazilim.proje_pazari.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Application statistics for admin analytics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationStatsDTO {

    @Schema(description = "Total number of applications")
    private long totalApplications;

    @Schema(description = "Applications by status distribution")
    private Map<String, Long> applicationsByStatus;

    @Schema(description = "Acceptance rate percentage")
    private double acceptanceRate;
}
