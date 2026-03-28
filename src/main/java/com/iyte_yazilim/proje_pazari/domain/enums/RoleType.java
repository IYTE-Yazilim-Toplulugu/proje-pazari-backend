package com.iyte_yazilim.proje_pazari.domain.enums;

import lombok.Getter;

@Getter
public enum RoleType {
    USER("Regular authenticated user"),
    ADMIN("Full system access");

    private final String description;

    RoleType(String description) {
        this.description = description;
    }
}
