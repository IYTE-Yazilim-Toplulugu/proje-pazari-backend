package com.iyte_yazilim.proje_pazari.domain.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Published after a password reset token is persisted; triggers the reset email after commit. */
@Getter
@AllArgsConstructor
public class PasswordResetEmailRequestedEvent {
    private final String userId;
    private final String email;
    private final String firstName;
    private final String resetLink;
}
