package code.with.vanilson.market.products.domain;

import code.with.vanilson.market.shared.domain.AggregateRoot;
import code.with.vanilson.market.shared.domain.Money;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends AggregateRoot {

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false, unique = true)
    private String sku;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "price_amount")),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "price_currency"))
    })
    private Money price;

    private int inventoryCount;

    public void decreaseInventory(int quantity) {
        if (this.inventoryCount < quantity) {
            throw new IllegalArgumentException("Insufficient inventory for Product: " + this.getId());
        }
        this.inventoryCount -= quantity;
    }

    public void increaseInventory(int quantity) {
        this.inventoryCount += quantity;
    }
}
