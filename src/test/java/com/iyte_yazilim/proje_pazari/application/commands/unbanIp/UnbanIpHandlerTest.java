package com.iyte_yazilim.proje_pazari.application.commands.unbanIp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.BannedIpRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UnbanIpHandlerTest {

    @Mock
    private BannedIpRepository bannedIpRepository;

    @InjectMocks
    private UnbanIpHandler handler;

    @Test
    @DisplayName("Should unban IP address successfully")
    void shouldUnbanIpSuccessfully() {
        when(bannedIpRepository.existsByIpAddress("192.168.1.100")).thenReturn(true);

        ApiResponse<Void> response = handler.handle(new UnbanIpCommand("192.168.1.100"));

        assertNotNull(response);
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        verify(bannedIpRepository).deleteByIpAddress("192.168.1.100");
    }

    @Test
    @DisplayName("Should return not found for IP not in ban list")
    void shouldReturnNotFoundForUnbannedIp() {
        when(bannedIpRepository.existsByIpAddress("10.0.0.1")).thenReturn(false);

        ApiResponse<Void> response = handler.handle(new UnbanIpCommand("10.0.0.1"));

        assertNotNull(response);
        assertEquals(ResponseCode.NOT_FOUND, response.getCode());
        verify(bannedIpRepository, never()).deleteByIpAddress(any());
    }

    @Test
    @DisplayName("Should return validation error for blank IP")
    void shouldReturnValidationErrorForBlankIp() {
        ApiResponse<Void> response = handler.handle(new UnbanIpCommand(""));

        assertNotNull(response);
        assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
    }

    @Test
    @DisplayName("Should return validation error for null IP")
    void shouldReturnValidationErrorForNullIp() {
        ApiResponse<Void> response = handler.handle(new UnbanIpCommand(null));

        assertNotNull(response);
        assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
    }
}
