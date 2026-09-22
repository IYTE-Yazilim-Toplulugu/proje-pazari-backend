package com.iyte_yazilim.proje_pazari.domain.events;

import com.github.f4b6a3.ulid.Ulid;

public class UserRegisteredEvent extends DomainEvent {

    private final Ulid userId;
    private final String email;
    private final String firstName;
    private final String verificationToken;

    public UserRegisteredEvent(
            Ulid userId, String email, String firstName, String verificationToken) {
        super(
                "UserRegistered",
                userId.toString(),
                "User",
                java.time.LocalDateTime.now().toString());
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.verificationToken = verificationToken;
    }

    public Ulid getUserId() {
        return this.userId;
    }

    public String getEmail() {
        return this.email;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getVerificationToken() {
        return this.verificationToken;
    }
}
