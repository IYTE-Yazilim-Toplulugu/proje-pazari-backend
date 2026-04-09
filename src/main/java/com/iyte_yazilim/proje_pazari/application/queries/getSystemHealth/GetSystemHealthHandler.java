package com.iyte_yazilim.proje_pazari.application.queries.getSystemHealth;

import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.dtos.SystemHealthDTO;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetSystemHealthHandler
        implements IRequestHandler<GetSystemHealthQuery, ApiResponse<SystemHealthDTO>> {

    private final DataSource dataSource;

    @Override
    public ApiResponse<SystemHealthDTO> handle(GetSystemHealthQuery query) {
        Map<String, String> services = new HashMap<>();

        // Check database
        try {
            dataSource.getConnection().close();
            services.put("database", "UP");
        } catch (Exception e) {
            services.put("database", "DOWN: " + e.getMessage());
        }

        // JVM memory info
        Runtime runtime = Runtime.getRuntime();
        Map<String, Long> memory = new HashMap<>();
        memory.put("totalMB", runtime.totalMemory() / (1024 * 1024));
        memory.put("freeMB", runtime.freeMemory() / (1024 * 1024));
        memory.put("usedMB", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
        memory.put("maxMB", runtime.maxMemory() / (1024 * 1024));

        // Uptime
        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        Duration uptime = Duration.ofMillis(uptimeMillis);
        String uptimeStr =
                String.format(
                        "%dd %dh %dm %ds",
                        uptime.toDays(),
                        uptime.toHoursPart(),
                        uptime.toMinutesPart(),
                        uptime.toSecondsPart());

        boolean allUp = services.values().stream().allMatch(s -> "UP".equals(s));
        String overallStatus = allUp ? "HEALTHY" : "DEGRADED";

        SystemHealthDTO healthDTO = new SystemHealthDTO(overallStatus, services, memory, uptimeStr);

        return ApiResponse.success(healthDTO, "System health retrieved successfully");
    }
}
