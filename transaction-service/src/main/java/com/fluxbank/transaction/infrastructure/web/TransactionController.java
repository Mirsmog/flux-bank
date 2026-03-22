package com.fluxbank.transaction.infrastructure.web;

import com.fluxbank.transaction.application.dto.*;
import com.fluxbank.transaction.application.service.TransactionService;
import com.fluxbank.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /** POST /api/v1/transactions/accounts/{accountId}/deposit */
    @PostMapping("/accounts/{accountId}/deposit")
    public ResponseEntity<ApiResponse<TransactionEventDto>> deposit(
            @PathVariable UUID accountId,
            @Valid @RequestBody RecordTransactionRequest request) {
        TransactionEventDto dto = transactionService.deposit(accountId, currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dto));
    }

    /** POST /api/v1/transactions/accounts/{accountId}/withdraw */
    @PostMapping("/accounts/{accountId}/withdraw")
    public ResponseEntity<ApiResponse<TransactionEventDto>> withdraw(
            @PathVariable UUID accountId,
            @Valid @RequestBody RecordTransactionRequest request) {
        TransactionEventDto dto = transactionService.withdraw(accountId, currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dto));
    }

    /** POST /api/v1/transactions/transfer */
    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionEventDto>> transfer(
            @Valid @RequestBody TransferRequest request) {
        RecordTransactionRequest txRequest = new RecordTransactionRequest(
                request.amount(),
                request.currency(),
                request.description(),
                null
        );
        TransactionEventDto dto = transactionService.transfer(
                request.fromAccountId(), request.toAccountId(), currentUserId(), txRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dto));
    }

    /** GET /api/v1/transactions/accounts/{accountId}/history?page=0&size=20 */
    @GetMapping("/accounts/{accountId}/history")
    public ResponseEntity<ApiResponse<TransactionHistoryResponse>> getAccountHistory(
            @PathVariable UUID accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        TransactionHistoryResponse response =
                transactionService.getAccountHistory(accountId, currentUserId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /** GET /api/v1/transactions/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionEventDto>> getTransaction(@PathVariable UUID id) {
        TransactionEventDto dto = transactionService.getTransaction(id, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    /** GET /api/v1/transactions/accounts/{accountId}/statement?from=...&to=... */
    @GetMapping("/accounts/{accountId}/statement")
    public ResponseEntity<ApiResponse<AccountStatementDto>> getAccountStatement(
            @PathVariable UUID accountId,
            @RequestParam Instant from,
            @RequestParam Instant to) {
        AccountStatementDto dto =
                transactionService.getAccountStatement(accountId, currentUserId(), from, to);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    private UUID currentUserId() {
        return UUID.fromString(
                SecurityContextHolder.getContext().getAuthentication().getName());
    }

    // ── Nested request record for transfer endpoint ───────────────────────────

    public record TransferRequest(
            @NotNull UUID fromAccountId,
            @NotNull UUID toAccountId,
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            @NotBlank @Size(max = 3) String currency,
            @Size(max = 500) String description
    ) {}
}
