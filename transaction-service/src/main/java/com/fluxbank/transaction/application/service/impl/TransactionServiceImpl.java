package com.fluxbank.transaction.application.service.impl;

import com.fluxbank.common.constants.KafkaTopics;
import com.fluxbank.common.exception.ErrorCode;
import com.fluxbank.common.exception.FluxBankException;
import com.fluxbank.transaction.application.dto.*;
import com.fluxbank.transaction.application.mapper.TransactionMapper;
import com.fluxbank.transaction.application.service.TransactionService;
import com.fluxbank.transaction.domain.event.TransactionRecordedEvent;
import com.fluxbank.transaction.domain.model.*;
import com.fluxbank.transaction.domain.repository.LedgerEntryRepository;
import com.fluxbank.transaction.domain.repository.TransactionEventRepository;
import com.fluxbank.transaction.infrastructure.client.AccountDetailsDto;
import com.fluxbank.transaction.infrastructure.client.AccountServiceClient;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionEventRepository transactionEventRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final TransactionMapper transactionMapper;
    private final AccountServiceClient accountServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional
    public TransactionEventDto deposit(UUID accountId, UUID userId, RecordTransactionRequest request) {
        AccountDetailsDto account = fetchAndValidateAccount(accountId, userId);
        validateCurrency(request.currency(), account.currency());

        BigDecimal currentBalance = account.balance();
        BigDecimal balanceAfter = currentBalance.add(request.amount());
        UUID correlationId = UUID.randomUUID();
        Instant now = Instant.now();

        TransactionEvent event = TransactionEvent.builder()
                .accountId(accountId)
                .correlationId(correlationId)
                .eventType(TransactionEventType.DEPOSIT)
                .status(TransactionStatus.COMPLETED)
                .amount(request.amount())
                .currency(request.currency())
                .balanceAfter(balanceAfter)
                .description(request.description())
                .referenceId(request.referenceId())
                .occurredAt(now)
                .build();

        event = transactionEventRepository.save(event);

        LedgerEntry ledgerEntry = LedgerEntry.builder()
                .transactionEventId(event.getId())
                .accountId(accountId)
                .entryType(LedgerEntryType.CREDIT)
                .amount(request.amount())
                .currency(request.currency())
                .correlationId(correlationId)
                .description(request.description())
                .occurredAt(now)
                .build();

        ledgerEntryRepository.save(ledgerEntry);

        log.info("Deposit recorded: accountId={}, correlationId={}, amount={}",
                accountId, correlationId, request.amount());

        publishEvent(new TransactionRecordedEvent(
                correlationId.toString(), accountId.toString(),
                TransactionEventType.DEPOSIT.name(), request.amount(), request.currency()));

        syncBalance(accountId, userId, request.amount());

        return transactionMapper.toDto(event);
    }

    @Override
    @Transactional
    public TransactionEventDto withdraw(UUID accountId, UUID userId, RecordTransactionRequest request) {
        AccountDetailsDto account = fetchAndValidateAccount(accountId, userId);
        validateCurrency(request.currency(), account.currency());

        BigDecimal currentBalance = account.balance();
        if (currentBalance.compareTo(request.amount()) < 0) {
            throw new FluxBankException(ErrorCode.VALIDATION_ERROR,
                    "Insufficient funds: available=" + currentBalance + ", requested=" + request.amount());
        }

        BigDecimal balanceAfter = currentBalance.subtract(request.amount());
        UUID correlationId = UUID.randomUUID();
        Instant now = Instant.now();

        TransactionEvent event = TransactionEvent.builder()
                .accountId(accountId)
                .correlationId(correlationId)
                .eventType(TransactionEventType.WITHDRAWAL)
                .status(TransactionStatus.COMPLETED)
                .amount(request.amount())
                .currency(request.currency())
                .balanceAfter(balanceAfter)
                .description(request.description())
                .referenceId(request.referenceId())
                .occurredAt(now)
                .build();

        event = transactionEventRepository.save(event);

        LedgerEntry ledgerEntry = LedgerEntry.builder()
                .transactionEventId(event.getId())
                .accountId(accountId)
                .entryType(LedgerEntryType.DEBIT)
                .amount(request.amount())
                .currency(request.currency())
                .correlationId(correlationId)
                .description(request.description())
                .occurredAt(now)
                .build();

        ledgerEntryRepository.save(ledgerEntry);

        log.info("Withdrawal recorded: accountId={}, correlationId={}, amount={}",
                accountId, correlationId, request.amount());

        publishEvent(new TransactionRecordedEvent(
                correlationId.toString(), accountId.toString(),
                TransactionEventType.WITHDRAWAL.name(), request.amount(), request.currency()));

        syncBalance(accountId, userId, request.amount().negate());

        return transactionMapper.toDto(event);
    }

    @Override
    @Transactional
    public TransactionEventDto transfer(UUID fromAccountId, UUID toAccountId, UUID userId,
                                        RecordTransactionRequest request) {
        if (fromAccountId.equals(toAccountId)) {
            throw new FluxBankException(ErrorCode.VALIDATION_ERROR,
                    "Transfer source and destination accounts must be different");
        }

        // Validate from-account (ownership check via X-User-Id)
        AccountDetailsDto fromAccount = fetchAndValidateAccount(fromAccountId, userId);
        validateCurrency(request.currency(), fromAccount.currency());

        // Validate to-account (no ownership check — recipient may be anyone)
        AccountDetailsDto toAccount = fetchAndValidateLookup(toAccountId, userId);
        validateCurrency(request.currency(), toAccount.currency());

        BigDecimal fromBalance = fromAccount.balance();
        if (fromBalance.compareTo(request.amount()) < 0) {
            throw new FluxBankException(ErrorCode.VALIDATION_ERROR,
                    "Insufficient funds: available=" + fromBalance + ", requested=" + request.amount());
        }

        UUID correlationId = UUID.randomUUID();
        Instant now = Instant.now();
        BigDecimal fromBalanceAfter = fromBalance.subtract(request.amount());
        BigDecimal toBalanceAfter = toAccount.balance().add(request.amount());

        // DEBIT leg — from account
        TransactionEvent debitEvent = TransactionEvent.builder()
                .accountId(fromAccountId)
                .counterpartAccountId(toAccountId)
                .correlationId(correlationId)
                .eventType(TransactionEventType.TRANSFER_DEBIT)
                .status(TransactionStatus.COMPLETED)
                .amount(request.amount())
                .currency(request.currency())
                .balanceAfter(fromBalanceAfter)
                .description(request.description())
                .referenceId(request.referenceId())
                .occurredAt(now)
                .build();

        debitEvent = transactionEventRepository.save(debitEvent);

        // CREDIT leg — to account
        TransactionEvent creditEvent = TransactionEvent.builder()
                .accountId(toAccountId)
                .counterpartAccountId(fromAccountId)
                .correlationId(correlationId)
                .eventType(TransactionEventType.TRANSFER_CREDIT)
                .status(TransactionStatus.COMPLETED)
                .amount(request.amount())
                .currency(request.currency())
                .balanceAfter(toBalanceAfter)
                .description(request.description())
                .occurredAt(now)
                .build();

        transactionEventRepository.save(creditEvent);

        // Ledger entries — double-entry accounting
        ledgerEntryRepository.save(LedgerEntry.builder()
                .transactionEventId(debitEvent.getId())
                .accountId(fromAccountId)
                .entryType(LedgerEntryType.DEBIT)
                .amount(request.amount())
                .currency(request.currency())
                .correlationId(correlationId)
                .description(request.description())
                .occurredAt(now)
                .build());

        ledgerEntryRepository.save(LedgerEntry.builder()
                .transactionEventId(creditEvent.getId())
                .accountId(toAccountId)
                .entryType(LedgerEntryType.CREDIT)
                .amount(request.amount())
                .currency(request.currency())
                .correlationId(correlationId)
                .description(request.description())
                .occurredAt(now)
                .build());

        log.info("Transfer recorded: correlationId={}, from={}, to={}, amount={}",
                correlationId, fromAccountId, toAccountId, request.amount());

        publishEvent(new TransactionRecordedEvent(
                correlationId.toString(), fromAccountId.toString(),
                TransactionEventType.TRANSFER_DEBIT.name(), request.amount(), request.currency()));

        syncBalance(fromAccountId, userId, request.amount().negate());
        syncBalance(toAccountId, userId, request.amount());

        return transactionMapper.toDto(debitEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionHistoryResponse getAccountHistory(UUID accountId, UUID userId, int page, int size) {
        // Validates account ownership
        fetchAndValidateAccount(accountId, userId);

        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionEvent> eventsPage =
                transactionEventRepository.findByAccountIdOrderByOccurredAtDesc(accountId, pageable);

        List<TransactionEventDto> dtos = eventsPage.getContent().stream()
                .map(transactionMapper::toDto)
                .toList();

        return TransactionHistoryResponse.builder()
                .accountId(accountId)
                .transactions(dtos)
                .page(page)
                .size(size)
                .totalElements(eventsPage.getTotalElements())
                .totalPages(eventsPage.getTotalPages())
                .hasNext(eventsPage.hasNext())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionEventDto getTransaction(UUID transactionId, UUID userId) {
        TransactionEvent event = transactionEventRepository.findById(transactionId)
                .orElseThrow(() -> new FluxBankException(ErrorCode.NOT_FOUND, "Transaction not found"));

        // Verify account belongs to userId
        fetchAndValidateAccount(event.getAccountId(), userId);

        return transactionMapper.toDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountStatementDto getAccountStatement(UUID accountId, UUID userId, Instant from, Instant to) {
        AccountDetailsDto account = fetchAndValidateAccount(accountId, userId);

        BigDecimal openingBalance = ledgerEntryRepository.computeBalanceBefore(
                accountId, from, LedgerEntryType.CREDIT);

        List<LedgerEntry> entries = ledgerEntryRepository
                .findByAccountIdAndOccurredAtBetweenOrderByOccurredAtAsc(accountId, from, to);

        BigDecimal netChange = entries.stream()
                .map(e -> e.getEntryType() == LedgerEntryType.CREDIT
                        ? e.getAmount()
                        : e.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal closingBalance = openingBalance.add(netChange);

        List<LedgerEntryDto> entryDtos = entries.stream()
                .map(transactionMapper::toLedgerDto)
                .toList();

        return AccountStatementDto.builder()
                .accountId(accountId)
                .accountNumber(account.accountNumber())
                .from(from)
                .to(to)
                .openingBalance(openingBalance)
                .closingBalance(closingBalance)
                .currency(account.currency())
                .entries(entryDtos)
                .build();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private AccountDetailsDto fetchAndValidateAccount(UUID accountId, UUID userId) {
        AccountDetailsDto account = accountServiceClient
                .getAccount(accountId, userId.toString())
                .getData();
        if (account == null) {
            throw new FluxBankException(ErrorCode.NOT_FOUND, "Account not found");
        }
        if (!"ACTIVE".equals(account.status())) {
            throw new FluxBankException(ErrorCode.VALIDATION_ERROR,
                    "Account " + accountId + " is not active (status=" + account.status() + ")");
        }
        return account;
    }

    private AccountDetailsDto fetchAndValidateLookup(UUID accountId, UUID userId) {
        AccountDetailsDto account = accountServiceClient
                .lookupAccount(accountId, userId.toString())
                .getData();
        if (account == null) {
            throw new FluxBankException(ErrorCode.NOT_FOUND,
                    "Destination account not found: " + accountId);
        }
        if (!"ACTIVE".equals(account.status())) {
            throw new FluxBankException(ErrorCode.VALIDATION_ERROR,
                    "Destination account " + accountId + " is not active (status=" + account.status() + ")");
        }
        return account;
    }

    private void validateCurrency(String requestCurrency, String accountCurrency) {
        if (!requestCurrency.equalsIgnoreCase(accountCurrency)) {
            throw new FluxBankException(ErrorCode.VALIDATION_ERROR,
                    "Currency mismatch: request=" + requestCurrency + ", account=" + accountCurrency);
        }
    }

    private void syncBalance(UUID accountId, UUID userId, BigDecimal delta) {
        try {
            accountServiceClient.applyBalanceDelta(
                    accountId, userId.toString(), Map.of("delta", delta));
        } catch (Exception e) {
            log.error("Failed to sync balance for accountId={}: {}", accountId, e.getMessage());
        }
    }

    private void publishEvent(TransactionRecordedEvent event) {
        try {
            kafkaTemplate.send(KafkaTopics.TRANSACTION_EVENTS, event.getAggregateId(), event);
        } catch (Exception e) {
            log.error("Failed to publish TransactionRecordedEvent [correlationId={}]: {}",
                    event.getAggregateId(), e.getMessage());
        }
    }
}
