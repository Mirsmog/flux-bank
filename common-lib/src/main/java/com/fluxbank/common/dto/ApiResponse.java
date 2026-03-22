package com.fluxbank.common.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * Generic API envelope returned by all flux-bank REST endpoints.
 *
 * <p>Successful responses set {@code success = true} and populate {@code data}.
 * Error responses set {@code success = false} and populate {@code error}.
 *
 * @param <T> the type of the response payload
 */
@Value
@Builder
public class ApiResponse<T> {

    boolean success;
    T data;
    ErrorDto error;

    @Builder.Default
    Instant timestamp = Instant.now();

    // ── Factory helpers ──────────────────────────────────────────────────────

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(ErrorDto error) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return error(ErrorDto.of(code, message));
    }
}
