package com.iyte_yazilim.proje_pazari;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ProjePazariApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjePazariApplication.class, args);
    }
}
