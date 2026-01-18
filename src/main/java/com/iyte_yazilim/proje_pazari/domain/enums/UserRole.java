package com.iyte_yazilim.proje_pazari.domain.enums;

public enum UserRole {
    APPLICANT("Can apply to projects"),
    PROJECT_OWNER("Can create and manage projects"),
    ADMIN("Full system access"),
    MODERATOR("Can moderate content");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
