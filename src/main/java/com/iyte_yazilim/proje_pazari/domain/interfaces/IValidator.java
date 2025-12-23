package com.iyte_yazilim.proje_pazari.domain.interfaces;

public interface IValidator<IRequest> {
    String[] validate(IRequest command);
}
