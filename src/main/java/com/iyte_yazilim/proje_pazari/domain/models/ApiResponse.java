package com.iyte_yazilim.proje_pazari.domain.models;

import lombok.Getter;

/**
 * ApiResponse
 * 
 * This class represents a generic API response structure.
 * It contains the data of type T, a message, and a status code.
 *
 * @param <T> the type of data contained in the response
 */
@Getter
public class ApiResponse<T> {
    private T data;
    private String message;
    private int code;

    public ApiResponse(T data, String message, int code) {
        this.data = data;
        this.message = message;
        this.code = code;
    }
}
