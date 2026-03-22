package com.fluxbank.payment.domain.model;

public enum PaymentStatus {
    INITIATED,
    VALIDATING,
    DEBITED,
    CREDITED,
    COMPLETED,
    FAILED,
    COMPENSATION_PENDING,
    COMPENSATED
}
