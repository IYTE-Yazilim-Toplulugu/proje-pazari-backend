package com.iyte_yazilim.proje_pazari.domain.entities;

import lombok.Getter;

@Getter
public enum RoleType {
    APPLICANT("Can apply to projects"),
    PROJECT_OWNER("Can create and manage projects"),
    ADMIN("Full system access");
    
    private final String description;

    // Constructor is required for enums with fields
    RoleType(String description) {
        this.description = description;
    }
}