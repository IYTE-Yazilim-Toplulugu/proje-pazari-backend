package com.example.demo.domain.interfaces;

public interface IValidator<IRequest> {
    String[] validate(IRequest command);
}
