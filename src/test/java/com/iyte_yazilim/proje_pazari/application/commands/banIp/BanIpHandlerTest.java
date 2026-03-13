package com.iyte_yazilim.proje_pazari.application.commands.banIp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.BannedIpRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.BannedIpEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BanIpHandlerTest {

    @Mock private BannedIpRepository bannedIpRepository;

    @InjectMocks private BanIpHandler handler;

    @Test
    @DisplayName("Should ban IP address successfully")
    void shouldBanIpSuccessfully() {
        when(bannedIpRepository.existsByIpAddress("192.168.1.100")).thenReturn(false);
        when(bannedIpRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BanIpCommand command = new BanIpCommand("192.168.1.100", "Spamming", null);
        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        verify(bannedIpRepository).save(any(BannedIpEntity.class));
    }

    @Test
    @DisplayName("Should ban IP with expiration date")
    void shouldBanIpWithExpiration() {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);
        when(bannedIpRepository.existsByIpAddress("10.0.0.1")).thenReturn(false);
        when(bannedIpRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BanIpCommand command = new BanIpCommand("10.0.0.1", "Temporary ban", expiresAt);
        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        verify(bannedIpRepository).save(any(BannedIpEntity.class));
    }

    @Test
    @DisplayName("Should return validation error for blank IP")
    void shouldReturnValidationErrorForBlankIp() {
        BanIpCommand command = new BanIpCommand("", "reason", null);
        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
        verify(bannedIpRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return validation error for null IP")
    void shouldReturnValidationErrorForNullIp() {
        BanIpCommand command = new BanIpCommand(null, "reason", null);
        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
    }

    @Test
    @DisplayName("Should return conflict error for already banned IP")
    void shouldReturnConflictForAlreadyBannedIp() {
        when(bannedIpRepository.existsByIpAddress("192.168.1.1")).thenReturn(true);

        BanIpCommand command = new BanIpCommand("192.168.1.1", "Already banned", null);
        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertEquals(ResponseCode.CONFLICT, response.getCode());
        verify(bannedIpRepository, never()).save(any());
    }
}
