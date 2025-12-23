package com.iyte_yazilim.proje_pazari.domain.entities;

import lombok.Getter;
import jakarta.persistence.MappedSuperclass;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
@SuppressWarnings("unused")
public abstract class AuditableEntity<TId> extends BaseEntity<TId> {

    private String createdBy;
    private String updatedBy;

}
