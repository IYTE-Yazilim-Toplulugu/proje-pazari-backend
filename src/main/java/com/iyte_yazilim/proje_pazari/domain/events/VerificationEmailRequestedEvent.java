package com.iyte_yazilim.proje_pazari.domain.events;

import com.github.f4b6a3.ulid.Ulid;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerificationEmailRequestedEvent {
    private final Ulid userId;
    private final String email;
    private final String firstName;
    private final String verificationToken;
}
