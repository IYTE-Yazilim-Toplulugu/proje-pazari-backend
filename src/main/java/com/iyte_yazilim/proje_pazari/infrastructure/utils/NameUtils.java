package com.iyte_yazilim.proje_pazari.infrastructure.utils;

public final class NameUtils {

    private NameUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static String buildFullName(String firstName, String lastName) {
        if (firstName == null && lastName == null) {
            return null;
        }
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }
}
