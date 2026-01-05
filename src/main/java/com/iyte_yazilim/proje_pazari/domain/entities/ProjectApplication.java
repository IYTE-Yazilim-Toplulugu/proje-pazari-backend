package com.iyte_yazilim.proje_pazari.domain.entities;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
public class ProjectApplication extends BaseEntity<Ulid> {

    private Project project;
    private User user;

    /** The current status of the application. Defaults to PENDING. */
    private ApplicationStatus status = ApplicationStatus.PENDING;
}
