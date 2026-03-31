package com.iyte_yazilim.proje_pazari.domain.interfaces;

/** Implemented by commands that carry a password and a confirmation field. */
public interface PasswordMatchable {
    String newPassword();

    String confirmPassword();
}
