package com.iyte_yazilim.proje_pazari.domain.exceptions;

import com.iyte_yazilim.proje_pazari.application.common.ErrorCode;

/**
 * A file was requested that does not exist in the configured storage backend.
 *
 * <p>Distinct from {@link FileValidationException} so a file that is simply missing maps to 404
 * rather than 400 — the two are reachable from the same call when a file disappears between an
 * existence check and the read that follows.
 *
 * <p>Named to avoid colliding with {@code java.io.FileNotFoundException}.
 */
public class StoredFileNotFoundException extends DomainException {
    public StoredFileNotFoundException(String path) {
        super(ErrorCode.FILE_NOT_FOUND, "File not found: " + path);
    }
}
