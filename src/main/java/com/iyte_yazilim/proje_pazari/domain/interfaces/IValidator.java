package com.iyte_yazilim.proje_pazari.domain.interfaces;

public interface IValidator<T> {

    /**
     * Validates the given command and returns an array of error messages if any.
     *
     * @param command the command to validate
     * @return an array of error messages; empty if no errors
     */
    String[] validate(T command);
}
