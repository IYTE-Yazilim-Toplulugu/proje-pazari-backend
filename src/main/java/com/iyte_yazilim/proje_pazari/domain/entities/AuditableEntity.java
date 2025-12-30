package com.iyte_yazilim.proje_pazari.domain.entities;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
@SuppressWarnings("unused")
public abstract class AuditableEntity<TId> extends BaseEntity<TId> {

    private String createdBy;
    private String updatedBy;
}
