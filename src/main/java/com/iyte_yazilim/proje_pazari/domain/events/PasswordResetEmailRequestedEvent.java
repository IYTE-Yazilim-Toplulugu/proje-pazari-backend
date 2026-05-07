package com.iyte_yazilim.proje_pazari.domain.events;

/** Published after a password reset token is persisted; triggers the reset email after commit. */
public class PasswordResetEmailRequestedEvent {
    private final String userId;
    private final String email;
    private final String firstName;
    private final String resetLink;

    public PasswordResetEmailRequestedEvent(
            String userId, String email, String firstName, String resetLink) {
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.resetLink = resetLink;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getEmail() {
        return this.email;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getResetLink() {
        return this.resetLink;
    }
}
