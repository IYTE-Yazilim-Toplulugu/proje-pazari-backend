package com.iyte_yazilim.proje_pazari.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@SuppressWarnings("unused")
public enum ProjectStatus {
    DRAFT(1),
    OPEN(2),
    IN_PROGRESS(3),
    COMPLETED(4),
    CANCELLED(5);

    private final int status;
}
