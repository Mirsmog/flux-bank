package com.fluxbank.card.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cards")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", nullable = false, columnDefinition = "card_type")
    private CardType type;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "card_status")
    @Builder.Default
    private CardStatus status = CardStatus.ACTIVE;

    @Column(name = "masked_pan", nullable = false, length = 19)
    private String maskedPan;

    @Column(name = "last_four_digits", nullable = false, length = 4)
    private String lastFourDigits;

    @Column(name = "cvv_hash", nullable = false, length = 60)
    private String cvvHash;

    @Column(name = "expiry_month", nullable = false)
    private Short expiryMonth;

    @Column(name = "expiry_year", nullable = false)
    private Short expiryYear;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Override
    public String toString() {
        return "Card{id=" + id + ", maskedPan=" + maskedPan + ", status=" + status + "}";
    }
}
