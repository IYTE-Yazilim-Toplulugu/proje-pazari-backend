package com.iyte_yazilim.proje_pazari.domain.models;

/**
 * Represents the outcome of resolving a file for download. Providers that expose a
 * pre-signed/public URL (e.g. MinIO/S3) should return {@link RedirectResult}. Providers that
 * cannot generate an externally-usable URL (e.g. local filesystem) must return
 * {@link InlineResult} so the controller can stream the bytes directly instead of redirecting
 * to itself.
 */
public sealed interface StorageDownloadResult
        permits StorageDownloadResult.RedirectResult, StorageDownloadResult.InlineResult {

    /** Adapter can produce a redirectable URL (e.g. MinIO/S3 presigned URL). */
    record RedirectResult(String url) implements StorageDownloadResult {}

    /** Adapter cannot produce a redirectable URL; file content is returned inline instead. */
    record InlineResult(byte[] content, String contentType, String filename)
            implements StorageDownloadResult {}
}