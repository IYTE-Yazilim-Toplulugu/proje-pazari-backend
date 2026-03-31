package com.iyte_yazilim.proje_pazari.application.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@SuppressWarnings("unused")
public class ProjectDto {
    private Long id;
    private Long userId;
    private String title;
    private String description;
}
