package code.with.vanilson.market.orders.application;

import code.with.vanilson.market.customers.domain.CustomerRepository;
import code.with.vanilson.market.events.domain.EventProducer;
import code.with.vanilson.market.orders.api.OrderDto;
import code.with.vanilson.market.orders.domain.Order;
import code.with.vanilson.market.orders.domain.OrderItem;
import code.with.vanilson.market.orders.domain.OrderRepository;
import code.with.vanilson.market.orders.domain.OrderStatus;
import code.with.vanilson.market.products.domain.Product;
import code.with.vanilson.market.products.domain.ProductRepository;
import code.with.vanilson.market.promotions.domain.Coupon;
import code.with.vanilson.market.promotions.domain.CouponRepository;
import code.with.vanilson.market.shared.domain.DomainException;
import code.with.vanilson.market.shared.domain.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final EventProducer eventProducer;

    @Transactional
    public OrderDto.Response placeOrder(OrderDto.CreateRequest request) {
        // Validate Customer
        if (!customerRepository.existsById(request.getCustomerId())) {
            throw new DomainException("Customer not found");
        }

        Order order = Order.builder()
                .tenantId(request.getTenantId())
                .customerId(request.getCustomerId())
                .status(OrderStatus.PLACED)
                .createdAt(Instant.now())
                .build();

        // Process Items
        for (OrderDto.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new DomainException("Product not found: " + itemRequest.getProductId()));

            // Domain Logic: Decrease Inventory
            product.decreaseInventory(itemRequest.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .sku(product.getSku())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();

            order.addItem(orderItem);
        }

        // Apply Coupon if present
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            Coupon coupon = couponRepository.findByCode(request.getCouponCode())
                    .orElseThrow(() -> new DomainException("Invalid coupon code"));

            if (!coupon.isValid()) {
                throw new DomainException("Coupon is expired or inactive");
            }

            order.applyCoupon(coupon.getCode(), coupon.getDiscount());
        }

        order = orderRepository.save(order);

        // Publish Rich Event
        eventProducer.publish("orders.placed", OrderPlacedEvent.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .totalAmount(order.getTotalAmount().getAmount())
                .currency(order.getTotalAmount().getCurrencyCode())
                .build());

        return mapToResponse(order);
    }

    @Transactional
    public void cancelOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DomainException("Order not found"));

        order.cancel();
        orderRepository.save(order);
        eventProducer.publish("orders.cancelled", "Order cancelled: " + orderId);
    }

    @Transactional
    public void settleOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DomainException("Order not found"));

        // Simplified Logic
        eventProducer.publish("orders.settled", "Order settled: " + orderId);
        // Maybe update status to SHIPPED or CONFIRMED
    }

    public OrderDto.Response getOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new DomainException("Order not found"));
        return mapToResponse(order);
    }

    private OrderDto.Response mapToResponse(Order order) {
        OrderDto.Response response = new OrderDto.Response();
        response.setId(order.getId());
        response.setCustomerId(order.getCustomerId());
        response.setStatus(order.getStatus().name());
        if (order.getTotalAmount() != null) {
            response.setTotalAmount(order.getTotalAmount().getAmount());
            response.setCurrency(order.getTotalAmount().getCurrencyCode());
        }
        response.setCreatedAt(order.getCreatedAt());
        response.setItems(order.getItems().stream().map(i -> {
            OrderDto.OrderItemResponse ir = new OrderDto.OrderItemResponse();
            ir.setProductId(i.getProductId());
            ir.setSku(i.getSku());
            ir.setQuantity(i.getQuantity());
            ir.setUnitPrice(i.getUnitPrice().getAmount());
            return ir;
        }).collect(Collectors.toList()));
        return response;
    }
}
