package com.fluxbank.payment.application.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fluxbank.common.constants.KafkaTopics;
import com.fluxbank.common.events.BaseEvent;
import com.fluxbank.common.exception.ErrorCode;
import com.fluxbank.common.exception.FluxBankException;
import com.fluxbank.payment.application.dto.InitiatePaymentRequest;
import com.fluxbank.payment.application.dto.PaymentDto;
import com.fluxbank.payment.application.dto.PaymentSummaryDto;
import com.fluxbank.payment.application.mapper.PaymentMapper;
import com.fluxbank.payment.application.service.PaymentService;
import com.fluxbank.payment.domain.event.PaymentCompletedEvent;
import com.fluxbank.payment.domain.event.PaymentFailedEvent;
import com.fluxbank.payment.domain.event.PaymentInitiatedEvent;
import com.fluxbank.payment.domain.model.OutboxEvent;
import com.fluxbank.payment.domain.model.Payment;
import com.fluxbank.payment.domain.model.PaymentStatus;
import com.fluxbank.payment.domain.model.PaymentType;
import com.fluxbank.payment.domain.repository.OutboxEventRepository;
import com.fluxbank.payment.domain.repository.PaymentRepository;
import com.fluxbank.payment.infrastructure.client.AccountServiceClient;
import com.fluxbank.payment.infrastructure.client.TransactionServiceClient;
import com.fluxbank.payment.infrastructure.client.dto.AccountDetailsDto;
import com.fluxbank.payment.infrastructure.client.dto.RecordTransactionRequest;
import com.fluxbank.payment.infrastructure.client.dto.TransactionEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final PaymentMapper paymentMapper;
    private final AccountServiceClient accountServiceClient;
    private final TransactionServiceClient transactionServiceClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public PaymentDto initiatePayment(UUID userId, String idempotencyKey, InitiatePaymentRequest request) {
        // Step 1: Idempotency check — return cached result immediately to avoid duplicate processing
        Optional<Payment> existing = paymentRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            log.info("Duplicate payment request detected: idempotencyKey={}", idempotencyKey);
            return paymentMapper.toDto(existing.get());
        }

        // Step 2: Validate accounts via Feign (before creating any DB records)
        AccountDetailsDto senderAccount = fetchAndValidateSenderAccount(userId, request);
        AccountDetailsDto receiverAccount = fetchAndValidateReceiverAccount(request, userId);

        // Step 3: Create payment record (INITIATED) and outbox event atomically
        Payment payment = Payment.builder()
                .idempotencyKey(idempotencyKey)
                .senderId(userId)
                .senderAccountId(request.senderAccountId())
                .receiverAccountId(request.receiverAccountId())
                .amount(request.amount())
                .currency(request.currency())
                .type(PaymentType.P2P_TRANSFER)
                .status(PaymentStatus.INITIATED)
                .description(request.description())
                .build();
        payment = paymentRepository.save(payment);

        log.info("Payment INITIATED: paymentId={}, idempotencyKey={}, senderId={}, amount={} {}",
                payment.getId(), idempotencyKey, userId, payment.getAmount(), payment.getCurrency());

        // Write outbox event in the same transaction — guarantees at-least-once delivery
        saveOutboxEvent(payment, new PaymentInitiatedEvent(
                payment.getId().toString(),
                payment.getSenderId().toString(),
                payment.getSenderAccountId().toString(),
                payment.getReceiverAccountId().toString(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getType().name()
        ));

        // Step 4: Execute saga synchronously within the same @Transactional boundary
        // so all intermediate state changes are consistent if a partial failure occurs.
        executePaymentSaga(payment, senderAccount, receiverAccount);

        return paymentMapper.toDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDto getPayment(UUID paymentId, UUID userId) {
        Payment payment = paymentRepository.findByIdAndSenderId(paymentId, userId)
                .orElseThrow(() -> new FluxBankException(ErrorCode.NOT_FOUND,
                        "Payment not found or access denied: id=" + paymentId));
        return paymentMapper.toDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentSummaryDto> getUserPayments(UUID userId) {
        return paymentRepository.findBySenderIdOrderByCreatedAtDesc(userId).stream()
                .map(paymentMapper::toSummaryDto)
                .toList();
    }

    // ── Validation helpers ────────────────────────────────────────────────────

    private AccountDetailsDto fetchAndValidateSenderAccount(UUID userId, InitiatePaymentRequest request) {
        AccountDetailsDto sender = accountServiceClient
                .getAccount(request.senderAccountId(), userId.toString())
                .getData();
        if (sender == null) {
            throw new FluxBankException(ErrorCode.NOT_FOUND, "Sender account not found");
        }
        if (!"ACTIVE".equals(sender.status())) {
            throw new FluxBankException(ErrorCode.VALIDATION_ERROR,
                    "Sender account is not active: status=" + sender.status());
        }
        if (!request.currency().equalsIgnoreCase(sender.currency())) {
            throw new FluxBankException(ErrorCode.VALIDATION_ERROR,
                    "Currency mismatch: requested=" + request.currency() + ", account=" + sender.currency());
        }
        if (sender.balance().compareTo(request.amount()) < 0) {
            throw new FluxBankException(ErrorCode.VALIDATION_ERROR,
                    "Insufficient funds: available=" + sender.balance() + ", requested=" + request.amount());
        }
        return sender;
    }

    private AccountDetailsDto fetchAndValidateReceiverAccount(InitiatePaymentRequest request, UUID userId) {
        AccountDetailsDto receiver = accountServiceClient
                .lookupAccount(request.receiverAccountId(), userId.toString())
                .getData();
        if (receiver == null) {
            throw new FluxBankException(ErrorCode.NOT_FOUND, "Receiver account not found");
        }
        if (!"ACTIVE".equals(receiver.status())) {
            throw new FluxBankException(ErrorCode.VALIDATION_ERROR,
                    "Receiver account is not active: status=" + receiver.status());
        }
        return receiver;
    }

    // ── Saga execution ────────────────────────────────────────────────────────

    private void executePaymentSaga(Payment payment,
                                    AccountDetailsDto senderAccount,
                                    AccountDetailsDto receiverAccount) {
        try {
            // VALIDATING
            payment.setStatus(PaymentStatus.VALIDATING);
            payment = paymentRepository.save(payment);
            log.info("Payment VALIDATING: paymentId={}", payment.getId());

            // DEBIT sender
            TransactionEventDto debitTx = transactionServiceClient.withdraw(
                    payment.getSenderAccountId(),
                    payment.getSenderId().toString(),
                    new RecordTransactionRequest(
                            payment.getAmount(),
                            payment.getCurrency(),
                            "Payment: " + payment.getDescription(),
                            payment.getId().toString()
                    )
            ).getData();

            payment.setStatus(PaymentStatus.DEBITED);
            payment.setDebitTransactionId(debitTx.id());
            payment = paymentRepository.save(payment);
            log.info("Payment DEBITED: paymentId={}, debitTxId={}", payment.getId(), debitTx.id());

            // CREDIT receiver (use senderId as the initiating user identity for the call)
            TransactionEventDto creditTx = transactionServiceClient.deposit(
                    payment.getReceiverAccountId(),
                    payment.getSenderId().toString(),
                    new RecordTransactionRequest(
                            payment.getAmount(),
                            payment.getCurrency(),
                            "Payment received: " + payment.getDescription(),
                            payment.getId().toString()
                    )
            ).getData();

            // Persist CREDITED state before transitioning to COMPLETED.
            // This ensures handleSagaFailure can correctly detect that both
            // sides of the transfer succeeded if the COMPLETED save fails.
            payment.setStatus(PaymentStatus.CREDITED);
            payment.setCreditTransactionId(creditTx.id());
            payment = paymentRepository.save(payment);

            // COMPLETED
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setCompletedAt(Instant.now());
            payment = paymentRepository.save(payment);

            log.info("Payment COMPLETED: paymentId={}, amount={} {}, debitTxId={}, creditTxId={}",
                    payment.getId(), payment.getAmount(), payment.getCurrency(),
                    debitTx.id(), creditTx.id());

            saveOutboxEvent(payment, new PaymentCompletedEvent(
                    payment.getId().toString(),
                    payment.getSenderId().toString(),
                    payment.getSenderAccountId().toString(),
                    payment.getReceiverAccountId().toString(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    debitTx.id().toString(),
                    creditTx.id().toString()
            ));

        } catch (Exception e) {
            log.error("Payment saga failed at step {}: paymentId={}, reason={}",
                    payment.getStatus(), payment.getId(), e.getMessage());
            handleSagaFailure(payment, e.getMessage());
        }
    }

    private void handleSagaFailure(Payment payment, String reason) {
        payment.setFailureReason(reason != null
                ? reason.substring(0, Math.min(reason.length(), 500))
                : "Unknown error");

        if (payment.getStatus() == PaymentStatus.CREDITED
                || payment.getStatus() == PaymentStatus.COMPLETED) {
            // Both debit and credit succeeded — money moved correctly.
            // Only the final status update failed, so recover to COMPLETED rather than compensating.
            log.warn("Payment reached CREDITED/COMPLETED but persistence failed — recovering: paymentId={}",
                    payment.getId());
            try {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setCompletedAt(Instant.now());
                payment = paymentRepository.save(payment);
                log.info("Payment COMPLETED (recovered): paymentId={}", payment.getId());
            } catch (Exception recoveryError) {
                log.error("CRITICAL: Could not recover COMPLETED state for paymentId={}: {}. Requires manual intervention.",
                        payment.getId(), recoveryError.getMessage());
                // Leave status as CREDITED so a manual recovery job can find and fix it.
            }
            // No failure outbox event — payment is logically complete.
            return;
        }

        if (payment.getStatus() == PaymentStatus.DEBITED) {
            // Debit was already applied — reverse it to maintain balance consistency
            payment.setStatus(PaymentStatus.COMPENSATION_PENDING);
            payment = paymentRepository.save(payment);
            log.info("Payment COMPENSATION_PENDING: paymentId={}", payment.getId());

            try {
                TransactionEventDto compensationTx = transactionServiceClient.deposit(
                        payment.getSenderAccountId(),
                        payment.getSenderId().toString(),
                        new RecordTransactionRequest(
                                payment.getAmount(),
                                payment.getCurrency(),
                                "Compensation for failed payment: " + payment.getId(),
                                "COMPENSATION_" + payment.getId()
                        )
                ).getData();

                payment.setCompensationTransactionId(compensationTx.id());
                payment.setStatus(PaymentStatus.COMPENSATED);
                log.info("Payment COMPENSATED: paymentId={}, compensationTxId={}",
                        payment.getId(), compensationTx.id());

            } catch (Exception compensationError) {
                log.error("CRITICAL: Compensation failed for paymentId={}: {}",
                        payment.getId(), compensationError.getMessage());
                // Keep status as COMPENSATION_PENDING — requires manual intervention
            }
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            log.info("Payment FAILED: paymentId={}, reason={}", payment.getId(), payment.getFailureReason());
        }

        payment = paymentRepository.save(payment);
        saveOutboxEvent(payment, new PaymentFailedEvent(
                payment.getId().toString(),
                payment.getSenderId().toString(),
                payment.getFailureReason(),
                payment.getStatus().name()
        ));
    }

    // ── Outbox helper ─────────────────────────────────────────────────────────

    private void saveOutboxEvent(Payment payment, BaseEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outbox = OutboxEvent.builder()
                    .aggregateId(payment.getId().toString())
                    .eventType(event.getEventType())
                    .topic(KafkaTopics.PAYMENT_EVENTS)
                    .payload(payload)
                    .build();
            outboxEventRepository.save(outbox);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox event for payment {}: {}",
                    payment.getId(), e.getMessage());
        }
    }
}
