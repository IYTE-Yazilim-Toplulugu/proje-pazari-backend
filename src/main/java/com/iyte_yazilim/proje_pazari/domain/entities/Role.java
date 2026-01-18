package com.iyte_yazilim.proje_pazari.domain.entities;

import com.iyte_yazilim.proje_pazari.domain.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role extends BaseEntity<String> {
    private UserRole name;

    private String description;
}
