package com.iyte_yazilim.proje_pazari.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailDto {
    private String to;
    private String subject;
    private String body;
}
