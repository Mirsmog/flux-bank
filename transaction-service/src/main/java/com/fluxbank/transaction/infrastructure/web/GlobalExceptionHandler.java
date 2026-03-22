package com.fluxbank.transaction.infrastructure.web;

import com.fluxbank.common.dto.ApiResponse;
import com.fluxbank.common.dto.ErrorDto;
import com.fluxbank.common.exception.ErrorCode;
import com.fluxbank.common.exception.FluxBankException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FluxBankException.class)
    public ResponseEntity<ApiResponse<Void>> handleFluxBankException(FluxBankException ex) {
        log.warn("Domain error [{}]: {}", ex.getErrorCode(), ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        ErrorDto error = ErrorDto.of(
                ErrorCode.VALIDATION_ERROR.getCode(),
                "Request validation failed",
                details
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(error));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unhandled exception", ex);
        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.INTERNAL_ERROR.getCode(),
                "An unexpected error occurred. Please try again later."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
