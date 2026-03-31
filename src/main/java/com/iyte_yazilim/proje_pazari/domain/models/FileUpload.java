package com.iyte_yazilim.proje_pazari.domain.models;

public record FileUpload(String filename, String contentType, byte[] bytes, long size) {}
