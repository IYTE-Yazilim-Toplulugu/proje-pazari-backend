package com.iyte_yazilim.proje_pazari.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.iyte_yazilim.proje_pazari.domain.enums.UserRole;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role extends BaseEntity<String> {

    
    @Column(unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole name;
    
    private String description;
}