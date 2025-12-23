package com.iyte_yazilim.proje_pazari.infrastructre.persistance.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "users")
@AllArgsConstructor
public class User {
    @Id
    private Long id;
    private String username;
    private String email;
    private String password;
}
