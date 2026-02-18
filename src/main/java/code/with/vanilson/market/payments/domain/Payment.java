package code.with.vanilson.market.payments.domain;

import code.with.vanilson.market.shared.domain.AggregateRoot;
import code.with.vanilson.market.shared.domain.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends AggregateRoot {

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private String tenantId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "amount")),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "currency"))
    })
    private Money amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
}
