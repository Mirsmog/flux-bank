package com.fluxbank.account.application.service.impl;

import com.fluxbank.account.application.dto.AccountDto;
import com.fluxbank.account.application.dto.AccountSummaryDto;
import com.fluxbank.account.application.dto.CreateAccountRequest;
import com.fluxbank.account.application.dto.UpdateAccountStatusRequest;
import com.fluxbank.account.application.mapper.AccountMapper;
import com.fluxbank.account.application.service.AccountNumberGenerator;
import com.fluxbank.account.application.service.AccountService;
import com.fluxbank.account.domain.event.AccountClosedEvent;
import com.fluxbank.account.domain.event.AccountOpenedEvent;
import com.fluxbank.account.domain.event.AccountStatusChangedEvent;
import com.fluxbank.account.domain.model.Account;
import com.fluxbank.account.domain.model.AccountStatus;
import com.fluxbank.account.domain.model.Money;
import com.fluxbank.account.domain.repository.AccountRepository;
import com.fluxbank.common.constants.KafkaTopics;
import com.fluxbank.common.events.BaseEvent;
import com.fluxbank.common.exception.ErrorCode;
import com.fluxbank.common.exception.FluxBankException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final AccountMapper accountMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional
    public AccountDto openAccount(UUID userId, CreateAccountRequest request) {
        String accountNumber = accountNumberGenerator.generate();
        Money zero = Money.zero(request.currency());

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .userId(userId)
                .type(request.type())
                .status(AccountStatus.ACTIVE)
                .name(request.name())
                .balance(zero)
                .reservedBalance(zero)
                .build();

        account = accountRepository.save(account);
        log.info("Account opened: accountId={}, userId={}", account.getId(), userId);

        publishEvent(new AccountOpenedEvent(
                account.getId().toString(),
                userId.toString(),
                accountNumber,
                request.type().name(),
                request.currency().name()
        ));

        return accountMapper.toDto(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountSummaryDto> getUserAccounts(UUID userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);
        return accountMapper.toSummaryDtoList(accounts);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDto getAccount(UUID accountId, UUID userId) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new FluxBankException(ErrorCode.NOT_FOUND, "Account not found"));
        return accountMapper.toDto(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDto getAccountByNumber(String accountNumber, UUID userId) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .filter(a -> a.getUserId().equals(userId))
                .orElseThrow(() -> new FluxBankException(ErrorCode.NOT_FOUND, "Account not found"));
        return accountMapper.toDto(account);
    }

    @Override
    @Transactional
    public AccountDto updateStatus(UUID accountId, UUID userId, UpdateAccountStatusRequest request) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new FluxBankException(ErrorCode.NOT_FOUND, "Account not found"));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new FluxBankException(ErrorCode.VALIDATION_ERROR,
                    "Cannot change status of a closed account");
        }

        if (request.status() == AccountStatus.CLOSED) {
            throw new FluxBankException(ErrorCode.VALIDATION_ERROR,
                    "Use DELETE /api/v1/accounts/{id} to close an account");
        }

        AccountStatus oldStatus = account.getStatus();
        account.setStatus(request.status());
        account = accountRepository.save(account);

        log.info("Account status updated: accountId={}, {} -> {}", accountId, oldStatus, request.status());

        publishEvent(new AccountStatusChangedEvent(
                account.getId().toString(),
                userId.toString(),
                oldStatus.name(),
                request.status().name()
        ));

        return accountMapper.toDto(account);
    }

    @Override
    @Transactional
    public void closeAccount(UUID accountId, UUID userId) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new FluxBankException(ErrorCode.NOT_FOUND, "Account not found"));

        if (!account.canClose()) {
            throw new FluxBankException(ErrorCode.VALIDATION_ERROR,
                    "Account must have zero balance to be closed");
        }

        account.setStatus(AccountStatus.CLOSED);
        account.setClosedAt(Instant.now());
        accountRepository.save(account);

        log.info("Account closed: accountId={}, userId={}", accountId, userId);

        publishEvent(new AccountClosedEvent(
                account.getId().toString(),
                userId.toString(),
                account.getAccountNumber()
        ));
    }

    private void publishEvent(BaseEvent event) {
        try {
            kafkaTemplate.send(KafkaTopics.ACCOUNT_EVENTS, event.getAggregateId(), event);
        } catch (Exception e) {
            log.error("Failed to publish event [{}]: {}", event.getEventType(), e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDto getAccountById(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new FluxBankException(ErrorCode.NOT_FOUND, "Account not found"));
        return accountMapper.toDto(account);
    }
}
