package com.iyte_yazilim.proje_pazari.infrastructure.persistence.converters;

import com.github.f4b6a3.ulid.Ulid;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for ULID type
 * Converts ULID to String for database storage and vice versa
 */
@Converter(autoApply = true)
public class UlidConverter implements AttributeConverter<Ulid, String> {

    @Override
    public String convertToDatabaseColumn(Ulid ulid) {
        return ulid == null ? null : ulid.toString();
    }

    @Override
    public Ulid convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Ulid.from(dbData);
    }
}
