package com.iyte_yazilim.proje_pazari.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "User statistics for admin analytics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatsDTO {

    @Schema(description = "Total number of users")
    private long totalUsers;

    @Schema(description = "Number of active users")
    private long activeUsers;

    @Schema(description = "Number of inactive users")
    private long inactiveUsers;

    @Schema(description = "Users by role distribution")
    private Map<String, Long> usersByRole;

    @Schema(description = "Users by email domain distribution")
    private Map<String, Long> usersByEmailDomain;
}
