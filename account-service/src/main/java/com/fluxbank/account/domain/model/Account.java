package com.fluxbank.account.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "account_number", unique = true, nullable = false, length = 16)
    private String accountNumber;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, columnDefinition = "account_type")
    private AccountType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "account_status")
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount",
                    column = @Column(name = "balance_amount", nullable = false, precision = 19, scale = 4)),
            @AttributeOverride(name = "currency",
                    column = @Column(name = "balance_currency", nullable = false, length = 3,
                            columnDefinition = "currency_code"))
    })
    private Money balance;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount",
                    column = @Column(name = "reserved_balance_amount", nullable = false, precision = 19, scale = 4)),
            @AttributeOverride(name = "currency",
                    column = @Column(name = "reserved_balance_currency", nullable = false, length = 3,
                            columnDefinition = "currency_code"))
    })
    private Money reservedBalance;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "closed_at")
    private Instant closedAt;

    /** Amount the user can actually spend (balance minus reserved funds). */
    public Money getAvailableBalance() {
        return balance.subtract(reservedBalance);
    }

    /** An account can be closed only when it is active and has a zero net balance. */
    public boolean canClose() {
        return status == AccountStatus.ACTIVE
                && balance.isZero()
                && reservedBalance.isZero();
    }

    @Override
    public String toString() {
        return "Account{id=" + id + ", accountNumber=" + accountNumber
                + ", userId=" + userId + ", type=" + type + ", status=" + status + "}";
    }
}
