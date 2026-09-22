package com.iyte_yazilim.proje_pazari.application.services;

public interface BanCheckService {

    boolean isIpBanned(String ip);

    void evict(String ip);
}
