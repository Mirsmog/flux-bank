package com.fluxbank.payment.application.service;

import com.fluxbank.payment.application.dto.InitiatePaymentRequest;
import com.fluxbank.payment.application.dto.PaymentDto;
import com.fluxbank.payment.application.dto.PaymentSummaryDto;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    PaymentDto initiatePayment(UUID userId, String idempotencyKey, InitiatePaymentRequest request);

    PaymentDto getPayment(UUID paymentId, UUID userId);

    List<PaymentSummaryDto> getUserPayments(UUID userId);
}
