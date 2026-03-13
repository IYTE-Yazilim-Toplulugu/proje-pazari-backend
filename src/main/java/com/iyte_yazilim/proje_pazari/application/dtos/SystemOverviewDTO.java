package com.iyte_yazilim.proje_pazari.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "System overview statistics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemOverviewDTO {

    @Schema(description = "Total number of registered users")
    private long totalUsers;

    @Schema(description = "Number of active (OPEN) projects")
    private long activeProjects;

    @Schema(description = "Number of pending applications")
    private long pendingApplications;

    @Schema(description = "New users registered today")
    private long newUsersToday;

    @Schema(description = "New users registered this week")
    private long newUsersThisWeek;

    @Schema(description = "New users registered this month")
    private long newUsersThisMonth;

    @Schema(description = "Total number of projects")
    private long totalProjects;

    @Schema(description = "Total number of applications")
    private long totalApplications;

    @Schema(description = "Users by role distribution")
    private Map<String, Long> usersByRole;
}
