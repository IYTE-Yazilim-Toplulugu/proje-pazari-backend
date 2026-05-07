package com.iyte_yazilim.proje_pazari.infrastructure.security.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.BannedIpRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.BannedIpEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BanCheckServiceImplTest {

    @Mock private BannedIpRepository bannedIpRepository;

    @InjectMocks private BanCheckServiceImpl banCheckService;

    @Test
    @DisplayName("isIpBanned returns true for permanently banned IP (no expiry)")
    void isIpBanned_permanentBan_returnsTrue() {
        BannedIpEntity ban = BannedIpEntity.builder().ipAddress("1.2.3.4").reason("spam").build();
        when(bannedIpRepository.findByIpAddress("1.2.3.4")).thenReturn(Optional.of(ban));

        assertTrue(banCheckService.isIpBanned("1.2.3.4"));
    }

    @Test
    @DisplayName("isIpBanned returns true for IP with future expiry")
    void isIpBanned_futureExpiry_returnsTrue() {
        BannedIpEntity ban =
                BannedIpEntity.builder()
                        .ipAddress("1.2.3.4")
                        .expiresAt(LocalDateTime.now().plusDays(1))
                        .build();
        when(bannedIpRepository.findByIpAddress("1.2.3.4")).thenReturn(Optional.of(ban));

        assertTrue(banCheckService.isIpBanned("1.2.3.4"));
    }

    @Test
    @DisplayName("isIpBanned returns false for IP with past expiry (expired ban)")
    void isIpBanned_expiredBan_returnsFalse() {
        BannedIpEntity ban =
                BannedIpEntity.builder()
                        .ipAddress("1.2.3.4")
                        .expiresAt(LocalDateTime.now().minusDays(1))
                        .build();
        when(bannedIpRepository.findByIpAddress("1.2.3.4")).thenReturn(Optional.of(ban));

        assertFalse(banCheckService.isIpBanned("1.2.3.4"));
    }

    @Test
    @DisplayName("isIpBanned returns false for unknown IP")
    void isIpBanned_unknownIp_returnsFalse() {
        when(bannedIpRepository.findByIpAddress("9.9.9.9")).thenReturn(Optional.empty());

        assertFalse(banCheckService.isIpBanned("9.9.9.9"));
    }

    @Test
    @DisplayName("evict completes without exception (annotation-driven)")
    void evict_doesNotThrow() {
        assertDoesNotThrow(() -> banCheckService.evict("1.2.3.4"));
    }
}
