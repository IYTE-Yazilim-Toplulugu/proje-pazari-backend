package com.iyte_yazilim.proje_pazari.application.commands.changePassword;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChangePasswordValidatorTest {

    private final ChangePasswordValidator validator = new ChangePasswordValidator();

    @Test
    @DisplayName("Returns no errors when new password differs from current")
    void validate_differentPasswords_returnsEmpty() {
        ChangePasswordCommand cmd =
                new ChangePasswordCommand("uid", "OldPass1!", "NewPass1!", "NewPass1!");
        assertArrayEquals(new String[0], validator.validate(cmd));
    }

    @Test
    @DisplayName("Returns error when new password equals current password")
    void validate_samePasswords_returnsError() {
        String samePass = "SamePass1!";
        ChangePasswordCommand cmd = new ChangePasswordCommand("uid", samePass, samePass, samePass);
        String[] errors = validator.validate(cmd);
        assertEquals(1, errors.length);
        assertTrue(errors[0].toLowerCase().contains("differ"));
    }

    @Test
    @DisplayName("Returns no errors when newPassword is null (null-safe)")
    void validate_nullNewPassword_returnsEmpty() {
        ChangePasswordCommand cmd = new ChangePasswordCommand("uid", "OldPass1!", null, null);
        assertArrayEquals(new String[0], validator.validate(cmd));
    }
}
