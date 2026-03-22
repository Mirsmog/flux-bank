package com.fluxbank.common.exception;

/**
 * Base runtime exception for all flux-bank domain and application errors.
 * All service-specific exceptions should extend this class.
 */
public class FluxBankException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;

    public FluxBankException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode.getCode();
        this.httpStatus = resolveHttpStatus(errorCode);
    }

    public FluxBankException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode.getCode();
        this.httpStatus = resolveHttpStatus(errorCode);
    }

    public FluxBankException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode.getCode();
        this.httpStatus = resolveHttpStatus(errorCode);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    private static int resolveHttpStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case UNAUTHORIZED -> 401;
            case FORBIDDEN -> 403;
            case NOT_FOUND -> 404;
            case VALIDATION_ERROR -> 422;
            case DUPLICATE_REQUEST -> 409;
            case INTERNAL_ERROR -> 500;
        };
    }
}
