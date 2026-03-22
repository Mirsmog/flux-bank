package com.fluxbank.common.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Structured error payload included in {@link ApiResponse} when {@code success} is {@code false}.
 */
@Value
@Builder
public class ErrorDto {

    /** Machine-readable error code (maps to {@link com.fluxbank.common.exception.ErrorCode#getCode()}). */
    String code;

    /** Human-readable description of the error. */
    String message;

    /** Optional field-level or contextual validation details. */
    List<String> details;

    public static ErrorDto of(String code, String message) {
        return ErrorDto.builder()
                .code(code)
                .message(message)
                .details(List.of())
                .build();
    }

    public static ErrorDto of(String code, String message, List<String> details) {
        return ErrorDto.builder()
                .code(code)
                .message(message)
                .details(details != null ? List.copyOf(details) : List.of())
                .build();
    }
}
