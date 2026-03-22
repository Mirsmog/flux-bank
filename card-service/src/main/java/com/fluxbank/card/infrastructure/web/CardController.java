package com.fluxbank.card.infrastructure.web;

import com.fluxbank.card.application.dto.BlockCardRequest;
import com.fluxbank.card.application.dto.CardDto;
import com.fluxbank.card.application.dto.CardSummaryDto;
import com.fluxbank.card.application.dto.IssueCardRequest;
import com.fluxbank.card.application.service.CardService;
import com.fluxbank.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping
    public ResponseEntity<ApiResponse<CardDto>> issueCard(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody IssueCardRequest request) {
        log.info("Issue card request: userId={}, accountId={}", userId, request.accountId());
        CardDto card = cardService.issueCard(UUID.fromString(userId), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(card));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CardSummaryDto>>> getUserCards(
            @AuthenticationPrincipal String userId) {
        List<CardSummaryDto> cards = cardService.getUserCards(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success(cards));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CardDto>> getCard(
            @PathVariable UUID id,
            @AuthenticationPrincipal String userId) {
        CardDto card = cardService.getCard(id, UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success(card));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<ApiResponse<List<CardSummaryDto>>> getAccountCards(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal String userId) {
        List<CardSummaryDto> cards = cardService.getAccountCards(accountId, UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success(cards));
    }

    @PatchMapping("/{id}/block")
    public ResponseEntity<ApiResponse<CardDto>> blockCard(
            @PathVariable UUID id,
            @AuthenticationPrincipal String userId,
            @RequestBody(required = false) BlockCardRequest blockRequest) {
        String reason = blockRequest != null ? blockRequest.reason() : null;
        CardDto card = cardService.blockCard(id, UUID.fromString(userId), reason);
        return ResponseEntity.ok(ApiResponse.success(card));
    }

    @PatchMapping("/{id}/unblock")
    public ResponseEntity<ApiResponse<CardDto>> unblockCard(
            @PathVariable UUID id,
            @AuthenticationPrincipal String userId) {
        CardDto card = cardService.unblockCard(id, UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success(card));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<CardDto>> closeCard(
            @PathVariable UUID id,
            @AuthenticationPrincipal String userId) {
        CardDto card = cardService.closeCard(id, UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success(card));
    }
}
