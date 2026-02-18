package code.with.vanilson.market.promotions.domain;

import code.with.vanilson.market.shared.domain.AggregateRoot;
import code.with.vanilson.market.shared.domain.Money;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "coupons")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon extends AggregateRoot {

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false, unique = true)
    private String code;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "discount_amount")),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "discount_currency"))
    })
    private Money discount;

    private Instant expiryDate;

    @Builder.Default
    private boolean active = true;

    public boolean isValid() {
        return active && (expiryDate == null || expiryDate.isAfter(Instant.now()));
    }
}
