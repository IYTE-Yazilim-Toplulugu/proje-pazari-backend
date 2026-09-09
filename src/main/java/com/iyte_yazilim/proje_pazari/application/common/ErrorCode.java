package com.iyte_yazilim.proje_pazari.application.common;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Stable, machine-readable error identifier returned in the {@code errorCode} field of {@link
 * ApiResponse}.
 *
 * <p>Unlike {@link ResponseCode} — which is a coarse HTTP-category bucket — {@code ErrorCode} is
 * granular: clients can branch reliably on a specific failure (e.g. distinguishing {@code
 * USER_NOT_FOUND} from {@code PROJECT_NOT_FOUND}, both of which map to the same {@code NOT_FOUND}
 * category).
 *
 * <p>Each constant carries:
 *
 * <ul>
 *   <li>a {@link ResponseCode} <b>category</b> — drives the numeric {@code code} field and the HTTP
 *       status of the response;
 *   <li>a <b>message key</b> — resolved via {@code MessageService} into a localized, user-safe
 *       message. Technical detail is never derived from this; it stays in server logs.
 * </ul>
 *
 * <p>The enum {@code name()} is the wire value (serialized via {@link JsonValue}); treat these
 * names as a public API contract and avoid renaming existing constants.
 */
@Getter
@AllArgsConstructor
@SuppressWarnings("unused")
public enum ErrorCode {

    // --- Request / validation errors ---
    /** Request body or parameters failed validation. */
    VALIDATION_ERROR(ResponseCode.VALIDATION_ERROR, "error.validation"),

    /** Request body could not be parsed (malformed JSON, wrong content type, etc.). */
    MALFORMED_REQUEST(ResponseCode.BAD_REQUEST, "error.bad.request"),

    /** A required request parameter was missing. */
    MISSING_PARAMETER(ResponseCode.BAD_REQUEST, "error.bad.request"),

    /** A supplied argument was rejected by domain or application logic. */
    INVALID_ARGUMENT(ResponseCode.BAD_REQUEST, "error.bad.request"),

    // --- Authentication / authorization errors ---
    /** Authentication is required or the supplied credentials are invalid. */
    UNAUTHORIZED(ResponseCode.UNAUTHORIZED, "error.unauthorized"),

    /** The authenticated principal lacks permission for the requested action. */
    ACCESS_DENIED(ResponseCode.FORBIDDEN, "error.forbidden"),

    /**
     * The supplied credentials are invalid (wrong email or password). Kept vague to prevent user
     * enumeration.
     */
    INVALID_CREDENTIALS(ResponseCode.BAD_REQUEST, "auth.login.failed"),

    /** The account exists but has been administratively deactivated. */
    ACCOUNT_DEACTIVATED(ResponseCode.FORBIDDEN, "auth.account.deactivated"),

    // --- Not-found errors ---
    /** No user matches the supplied identifier. */
    USER_NOT_FOUND(ResponseCode.NOT_FOUND, "user.not.found"),

    /** No project matches the supplied identifier. */
    PROJECT_NOT_FOUND(ResponseCode.NOT_FOUND, "project.not.found"),

    /** No project application matches the supplied identifier. */
    APPLICATION_NOT_FOUND(ResponseCode.NOT_FOUND, "application.not.found"),

    /** No flagged content matches the supplied identifier. */
    FLAGGED_CONTENT_NOT_FOUND(ResponseCode.NOT_FOUND, "flag.not.found"),

    /** A user has already submitted an application to the same project. */
    APPLICATION_ALREADY_EXISTS(ResponseCode.CONFLICT, "application.already.exists"),

    // --- Email / verification errors ---
    /** The account's email address has not been verified yet. */
    EMAIL_NOT_VERIFIED(ResponseCode.FORBIDDEN, "auth.email.not.verified"),

    /** The account's email address is already verified. */
    EMAIL_ALREADY_VERIFIED(ResponseCode.CONFLICT, "auth.email.already.verified.error"),

    /** The verification token has expired. */
    VERIFICATION_TOKEN_EXPIRED(ResponseCode.BAD_REQUEST, "auth.verification.token.expired"),

    /** The verification token is invalid or already used. */
    INVALID_VERIFICATION_TOKEN(ResponseCode.BAD_REQUEST, "auth.verification.token.invalid"),

    /** Sending an email failed. */
    EMAIL_SEND_FAILED(ResponseCode.INTERNAL_SERVER_ERROR, "error.internal"),

    // --- File errors ---
    /** Uploaded file exceeds the maximum allowed size. */
    FILE_TOO_LARGE(ResponseCode.VALIDATION_ERROR, "validation.file.size.exceeded"),

    /** Uploaded file failed validation (wrong type, corrupt, etc.). */
    FILE_VALIDATION_FAILED(ResponseCode.BAD_REQUEST, "validation.file.type.invalid"),

    /** A file storage operation failed. */
    FILE_STORAGE_ERROR(ResponseCode.INTERNAL_SERVER_ERROR, "error.internal"),

    /** The requested file does not exist in the storage backend. */
    FILE_NOT_FOUND(ResponseCode.NOT_FOUND, "file.not.found"),

    // --- Domain state errors ---
    /** A user lifecycle method was invoked from an invalid current state. */
    ILLEGAL_USER_STATE(ResponseCode.CONFLICT, "user.illegal.state"),

    /** A project-application workflow method was invoked from an invalid current status. */
    ILLEGAL_APPLICATION_STATE(ResponseCode.CONFLICT, "application.illegal.state"),

    /** The project's current state prevents an application approval. */
    PROJECT_CANNOT_ACCEPT_APPLICATIONS(ResponseCode.CONFLICT, "project.cannot.accept.applications"),

    /** A concurrent review prevented a deterministic application transition. */
    APPLICATION_REVIEW_CONFLICT(ResponseCode.CONFLICT, "application.review.conflict"),

    // --- Catch-all ---
    /** An unexpected, unclassified server error occurred. */
    INTERNAL_ERROR(ResponseCode.INTERNAL_SERVER_ERROR, "error.internal");

    /** Coarse HTTP-category bucket; drives the numeric {@code code} field and the HTTP status. */
    private final ResponseCode category;

    /** Message-bundle key resolved into a localized, user-safe message. */
    private final String messageKey;

    /** The stable wire value of this error code (its {@code name()}). */
    @JsonValue
    public String getValue() {
        return name();
    }
}
