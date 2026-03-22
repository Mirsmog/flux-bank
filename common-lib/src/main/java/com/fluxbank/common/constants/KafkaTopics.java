package com.fluxbank.common.constants;

/**
 * Centralized Kafka topic name constants shared across all flux-bank services.
 *
 * <p>All producers and consumers must reference these constants rather than
 * hard-coding topic strings, ensuring naming consistency across the platform.
 */
public final class KafkaTopics {

    private KafkaTopics() {
        throw new UnsupportedOperationException("KafkaTopics is a constants class");
    }

    // ── Auth service ──────────────────────────────────────────────────────────
    public static final String AUTH_EVENTS = "fluxbank.auth.events";

    // ── Account service ───────────────────────────────────────────────────────
    public static final String ACCOUNT_EVENTS = "fluxbank.account.events";

    // ── Transaction service ───────────────────────────────────────────────────
    public static final String TRANSACTION_EVENTS = "fluxbank.transaction.events";

    // ── Payment service ───────────────────────────────────────────────────────
    public static final String PAYMENT_EVENTS = "fluxbank.payment.events";

    // ── Notification service ──────────────────────────────────────────────────
    public static final String NOTIFICATION_EVENTS = "fluxbank.notification.events";

    // ── Card service ──────────────────────────────────────────────────────────
    public static final String CARD_EVENTS = "fluxbank.card.events";

    // ── KYC service ───────────────────────────────────────────────────────────
    public static final String KYC_EVENTS = "fluxbank.kyc.events";
}
