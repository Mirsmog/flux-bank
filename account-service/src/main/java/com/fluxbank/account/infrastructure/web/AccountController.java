package com.fluxbank.account.infrastructure.web;

import com.fluxbank.account.application.dto.AccountDto;
import com.fluxbank.account.application.dto.AccountSummaryDto;
import com.fluxbank.account.application.dto.ApplyBalanceDeltaRequest;
import com.fluxbank.account.application.dto.CreateAccountRequest;
import com.fluxbank.account.application.dto.UpdateAccountStatusRequest;
import com.fluxbank.account.application.service.AccountService;
import com.fluxbank.common.dto.ApiResponse;
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
@RequestMapping("/api/v1/accounts")
@Tag(name = "Accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<ApiResponse<AccountDto>> openAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        AccountDto dto = accountService.openAccount(currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountSummaryDto>>> getUserAccounts() {
        List<AccountSummaryDto> accounts = accountService.getUserAccounts(currentUserId());
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountDto>> getAccount(@PathVariable UUID id) {
        AccountDto dto = accountService.getAccount(id, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountDto>> getAccountByNumber(
            @PathVariable String accountNumber) {
        AccountDto dto = accountService.getAccountByNumber(accountNumber, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AccountDto>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAccountStatusRequest request) {
        AccountDto dto = accountService.updateStatus(id, currentUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> closeAccount(@PathVariable UUID id) {
        accountService.closeAccount(id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Internal lookup endpoint — no ownership check.
     * Used by transaction-service to validate a recipient account in transfers.
     */
    @GetMapping("/{id}/lookup")
    public ResponseEntity<ApiResponse<AccountDto>> lookupAccount(@PathVariable UUID id) {
        AccountDto dto = accountService.getAccountById(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    /**
     * Internal balance update endpoint — called by transaction-service to sync balances
     * after recording a transaction event. Positive delta = credit; negative = debit.
     */
    @PutMapping("/{id}/balance")
    public ResponseEntity<ApiResponse<AccountDto>> applyBalanceDelta(
            @PathVariable UUID id,
            @Valid @RequestBody ApplyBalanceDeltaRequest request) {
        AccountDto dto = accountService.applyBalanceDelta(id, request.delta());
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    private UUID currentUserId() {
        return UUID.fromString(
                SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
