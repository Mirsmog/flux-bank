package com.fluxbank.payment.domain.model;

public enum PaymentType {
    P2P_TRANSFER,   // user-to-user transfer
    DEPOSIT,        // incoming from external
    WITHDRAWAL      // outgoing to external
}
