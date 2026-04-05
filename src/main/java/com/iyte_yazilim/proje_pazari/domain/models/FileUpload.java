package com.iyte_yazilim.proje_pazari.domain.models;

public record FileUpload(String filename, String contentType, byte[] bytes, long size) {


    public FileUpload(String filename, String contentType, byte[] bytes, long size) {
        if (bytes != null && size != bytes.length) {
            throw new IllegalArgumentException("size must be equal to bytes.length");
        }
        this.filename = filename;
        this.contentType = contentType;
        this.bytes = bytes != null ? bytes.clone() : null;
        this.size = size;
    }

    @Override
    public byte[] bytes() {
        return bytes != null ? bytes.clone() : null;
    }

}
