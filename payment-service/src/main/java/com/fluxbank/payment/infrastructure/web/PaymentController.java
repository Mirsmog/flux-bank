package com.fluxbank.payment.infrastructure.web;

import com.fluxbank.common.dto.ApiResponse;
import com.fluxbank.common.exception.ErrorCode;
import com.fluxbank.payment.application.dto.InitiatePaymentRequest;
import com.fluxbank.payment.application.dto.PaymentDto;
import com.fluxbank.payment.application.dto.PaymentSummaryDto;
import com.fluxbank.payment.application.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /** POST /api/v1/payments — initiate a new payment (idempotent via X-Idempotency-Key). */
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentDto>> initiatePayment(
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody InitiatePaymentRequest request) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(
                            ErrorCode.VALIDATION_ERROR.getCode(),
                            "X-Idempotency-Key header is required"));
        }

        PaymentDto dto = paymentService.initiatePayment(currentUserId(), idempotencyKey, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dto));
    }

    /** GET /api/v1/payments — list all payments initiated by the authenticated user. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentSummaryDto>>> getUserPayments() {
        List<PaymentSummaryDto> payments = paymentService.getUserPayments(currentUserId());
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    /** GET /api/v1/payments/{id} — get a single payment by ID (ownership enforced). */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentDto>> getPayment(@PathVariable UUID id) {
        PaymentDto dto = paymentService.getPayment(id, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    private UUID currentUserId() {
        return UUID.fromString(
                SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
