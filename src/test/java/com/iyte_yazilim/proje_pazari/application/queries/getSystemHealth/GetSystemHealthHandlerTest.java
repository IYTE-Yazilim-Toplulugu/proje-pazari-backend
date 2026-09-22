package com.iyte_yazilim.proje_pazari.application.queries.getSystemHealth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.dtos.SystemHealthDTO;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetSystemHealthHandlerTest {

    @Mock private DataSource dataSource;
    @Mock private Connection connection;

    @InjectMocks private GetSystemHealthHandler handler;

    @Test
    @DisplayName("Should return healthy status when database is available")
    void shouldReturnHealthyWhenDatabaseIsAvailable() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);

        ApiResponse<SystemHealthDTO> response = handler.handle(new GetSystemHealthQuery());

        assertNotNull(response.getData());
        assertEquals("HEALTHY", response.getData().status());
        assertEquals("UP", response.getData().services().get("database"));
        assertNotNull(response.getData().memory());
        assertNotNull(response.getData().uptime());
        verify(connection).close();
    }

    @Test
    @DisplayName("Should return degraded status when database is down")
    void shouldReturnDegradedWhenDatabaseIsDown() throws Exception {
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection refused"));

        ApiResponse<SystemHealthDTO> response = handler.handle(new GetSystemHealthQuery());

        assertNotNull(response.getData());
        assertEquals("DEGRADED", response.getData().status());
        assertTrue(response.getData().services().get("database").startsWith("DOWN"));
    }

    @Test
    @DisplayName("Should include memory metrics")
    void shouldIncludeMemoryMetrics() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);

        ApiResponse<SystemHealthDTO> response = handler.handle(new GetSystemHealthQuery());

        assertNotNull(response.getData().memory());
        assertTrue(response.getData().memory().containsKey("totalMB"));
        assertTrue(response.getData().memory().containsKey("freeMB"));
        assertTrue(response.getData().memory().containsKey("usedMB"));
        assertTrue(response.getData().memory().containsKey("maxMB"));
    }
}
