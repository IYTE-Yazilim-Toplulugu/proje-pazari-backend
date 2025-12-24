package com.iyte_yazilim.proje_pazari.domain.entities;

import com.github.f4b6a3.ulid.Ulid;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("unused")
public class User extends BaseEntity<Ulid> {

    private String email;
    private String password;
    private String firstName;
    private String lastName;

    private String description;

    private String profilePictureUrl;

    private String linkedinUrl;
    private String githubUrl;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
