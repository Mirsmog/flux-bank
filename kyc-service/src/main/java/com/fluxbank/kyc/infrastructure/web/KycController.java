package com.fluxbank.kyc.infrastructure.web;

import com.fluxbank.common.dto.ApiResponse;
import com.fluxbank.common.exception.ErrorCode;
import com.fluxbank.common.exception.FluxBankException;
import com.fluxbank.kyc.application.dto.KycDto;
import com.fluxbank.kyc.application.dto.ReviewKycRequest;
import com.fluxbank.kyc.application.dto.SubmitKycRequest;
import com.fluxbank.kyc.application.service.KycService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;

    @PostMapping
    public ResponseEntity<ApiResponse<KycDto>> submitKyc(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody SubmitKycRequest request) {
        log.info("KYC submission: userId={}", userId);
        KycDto kyc = kycService.submitKyc(UUID.fromString(userId), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(kyc));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<KycDto>> getMyKyc(
            @AuthenticationPrincipal String userId) {
        KycDto kyc = kycService.getKyc(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success(kyc));
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<ApiResponse<KycDto>> reviewKyc(
            @PathVariable UUID id,
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody ReviewKycRequest request) {
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (!isAdmin) {
            throw new FluxBankException(ErrorCode.UNAUTHORIZED, "Admin access required for KYC review.");
        }
        KycDto kyc = kycService.reviewKyc(id, request);
        return ResponseEntity.ok(ApiResponse.success(kyc));
    }
}
