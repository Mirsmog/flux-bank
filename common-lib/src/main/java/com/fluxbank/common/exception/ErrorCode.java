package com.fluxbank.common.exception;

/**
 * Enumeration of well-known error codes used across all flux-bank services.
 * Services should use these codes when throwing {@link FluxBankException}
 * so that API consumers receive consistent, machine-readable error identifiers.
 */
public enum ErrorCode {

    UNAUTHORIZED("AUTH_001", "Authentication required"),
    FORBIDDEN("AUTH_002", "Insufficient permissions to perform this operation"),
    NOT_FOUND("RESOURCE_001", "The requested resource was not found"),
    VALIDATION_ERROR("VALIDATION_001", "One or more request fields are invalid"),
    INTERNAL_ERROR("SYSTEM_001", "An unexpected internal error occurred"),
    DUPLICATE_REQUEST("IDEMPOTENCY_001", "A duplicate request was detected and rejected");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
