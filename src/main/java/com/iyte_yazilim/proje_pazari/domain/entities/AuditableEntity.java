package com.iyte_yazilim.proje_pazari.domain.entities;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract base class that extends {@link BaseEntity} with audit trail fields.
 *
 * <p>Provides tracking of who created and last modified an entity. Entities that require full audit
 * trail should extend this class.
 *
 * <p>Fields inherited from {@link BaseEntity}:
 *
 * <ul>
 *   <li>{@code id} - Unique identifier
 *   <li>{@code createdAt} - Creation timestamp
 *   <li>{@code updatedAt} - Last modification timestamp
 * </ul>
 *
 * @param <TId> the type of the entity identifier
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2024-01-01
 * @see BaseEntity
 */
@Getter
@Setter
@MappedSuperclass
@SuppressWarnings("unused")
public abstract class AuditableEntity<TId> extends BaseEntity<TId> {

    /** Identifier of the user who created this entity. */
    private String createdBy;

    /** Identifier of the user who last updated this entity. */
    private String updatedBy;
}
