package com.iyte_yazilim.proje_pazari.domain.interfaces;

public interface IValidator<T> {
    String[] validate(T command);
}
