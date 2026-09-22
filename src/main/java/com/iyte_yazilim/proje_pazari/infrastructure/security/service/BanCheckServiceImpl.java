package com.iyte_yazilim.proje_pazari.infrastructure.security.service;

import com.iyte_yazilim.proje_pazari.application.services.BanCheckService;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.BannedIpRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BanCheckServiceImpl implements BanCheckService {

    private final BannedIpRepository bannedIpRepository;

    @Override
    @Cacheable(value = "ip-ban-cache", key = "#ip")
    public boolean isIpBanned(String ip) {
        return bannedIpRepository
                .findByIpAddress(ip)
                .map(
                        ban ->
                                ban.getExpiresAt() == null
                                        || ban.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    @Override
    @CacheEvict(value = "ip-ban-cache", key = "#ip")
    public void evict(String ip) {
        // annotation-driven cache eviction
    }
}
