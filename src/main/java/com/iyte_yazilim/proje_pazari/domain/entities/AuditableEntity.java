package com.iyte_yazilim.proje_pazari.domain.entities;

import lombok.Getter;
import javax.persistence.MappedSuperclass;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class AuditableEntity extends BaseEntity {

    private String createdBy;
    private String updatedBy;

}
