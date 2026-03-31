package com.iyte_yazilim.proje_pazari.application.validators;

/** Implemented by commands that carry a password and a confirmation field. */
public interface PasswordMatchable {
    String newPassword();

    String confirmPassword();
}
