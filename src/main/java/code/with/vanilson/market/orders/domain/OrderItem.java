package code.with.vanilson.market.orders.domain;

import code.with.vanilson.market.shared.domain.BaseEntity;
import code.with.vanilson.market.shared.domain.Money;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem extends BaseEntity {

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "unit_price_amount")),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "unit_price_currency"))
    })
    private Money unitPrice;

    public Money subTotal() {
        return unitPrice.multiply(quantity);
    }
}
