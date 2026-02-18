package code.with.vanilson.market.shared.domain;

import code.with.vanilson.market.shared.infrastructure.exception.CurrencyMismatchException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA
public class Money {
    private BigDecimal amount;
    private String currencyCode;

    public Money(BigDecimal amount, String currencyCode) {
        this.amount = amount.setScale(2, RoundingMode.HALF_EVEN);
        this.currencyCode = currencyCode;
    }

    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, currencyCode);
    }

    public static Money zero(String currencyCode) {
        return new Money(BigDecimal.ZERO, currencyCode);
    }

    public Money add(Money other) {
        if (!this.currencyCode.equals(other.currencyCode)) {
            throw new CurrencyMismatchException("Cannot add money with different currencies");
        }
        return new Money(this.amount.add(other.amount), this.currencyCode);
    }

    public Money subtract(Money other) {
        if (!this.currencyCode.equals(other.currencyCode)) {
            throw new CurrencyMismatchException("Cannot subtract money with different currencies");
        }
        return new Money(this.amount.subtract(other.amount), this.currencyCode);
    }

    public Money multiply(int factor) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(factor)), this.currencyCode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount) && Objects.equals(currencyCode, money.currencyCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currencyCode);
    }
}
