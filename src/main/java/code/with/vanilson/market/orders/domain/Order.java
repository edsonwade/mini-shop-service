package code.with.vanilson.market.orders.domain;

import code.with.vanilson.market.shared.domain.AggregateRoot;
import code.with.vanilson.market.shared.domain.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends AggregateRoot {

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "total_amount")),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "total_currency"))
    })
    private Money totalAmount;

    @Column(name = "coupon_code")
    private String couponCode;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "discount_amount")),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "discount_currency"))
    })
    private Money discountAmount;

    public void addItem(OrderItem item) {
        this.items.add(item);
        calculateTotal();
    }

    public void calculateTotal() {
        if (items.isEmpty()) {
            this.totalAmount = null; // Will be set when items are added
            return;
        }

        // Check if first item has valid unitPrice
        if (items.get(0).getUnitPrice() == null) {
            throw new IllegalStateException("Order item unit price cannot be null");
        }

        Money total = items.get(0).subTotal();
        for (int i = 1; i < items.size(); i++) {
            if (items.get(i).getUnitPrice() == null) {
                throw new IllegalStateException("Order item unit price cannot be null");
            }
            total = total.add(items.get(i).subTotal());
        }

        if (discountAmount != null) {
            total = total.subtract(discountAmount);
        }

        this.totalAmount = total;
    }

    public void applyCoupon(String code, Money discount) {
        this.couponCode = code;
        this.discountAmount = discount;
        calculateTotal();
    }

    public void markPaid() {
        if (this.status == OrderStatus.PLACED) {
            this.status = OrderStatus.PAID;
        }
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
        // Logic to return items to inventory would go here (via Domain Event usually)
    }

    public void confirmPayment() {
        if (this.status == OrderStatus.PLACED) {
            this.status = OrderStatus.PAID;
        }
    }
}
