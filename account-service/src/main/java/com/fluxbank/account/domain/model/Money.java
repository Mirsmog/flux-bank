package com.fluxbank.account.domain.model;

import com.fluxbank.common.exception.ErrorCode;
import com.fluxbank.common.exception.FluxBankException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Immutable monetary value object pairing an amount with its currency.
 * Fields are non-final to satisfy JPA's proxy requirements; no setters are
 * exposed so the class is effectively immutable from outside this package.
 */
@Embeddable
@Getter
@EqualsAndHashCode
public class Money {

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3, columnDefinition = "currency_code")
    private Currency currency;

    /** For JPA only — do not call directly. */
    protected Money() {}

    private Money(BigDecimal amount, Currency currency) {
        this.amount = amount.setScale(4, RoundingMode.HALF_UP);
        this.currency = currency;
    }

    public static Money of(BigDecimal amount, Currency currency) {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        return new Money(amount, currency);
    }

    public static Money zero(Currency currency) {
        Objects.requireNonNull(currency, "currency must not be null");
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public int compareTo(Money other) {
        assertSameCurrency(other);
        return this.amount.compareTo(other.amount);
    }

    @Override
    public String toString() {
        return amount.toPlainString() + " " + currency;
    }

    private void assertSameCurrency(Money other) {
        Objects.requireNonNull(other, "other Money must not be null");
        if (!this.currency.equals(other.currency)) {
            throw new FluxBankException(
                    ErrorCode.VALIDATION_ERROR,
                    "Currency mismatch: " + this.currency + " vs " + other.currency
            );
        }
    }
}
