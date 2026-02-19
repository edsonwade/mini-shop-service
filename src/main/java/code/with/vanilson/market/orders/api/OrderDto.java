package code.with.vanilson.market.orders.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class OrderDto {

    @Data
    @io.swagger.v3.oas.annotations.media.Schema(description = "Request to place a new order")
    public static class CreateRequest {
        @NotBlank
        @io.swagger.v3.oas.annotations.media.Schema(description = "Tenant identifier", example = "tenant-001")
        private String tenantId;
        @NotNull
        @io.swagger.v3.oas.annotations.media.Schema(description = "ID of the customer placing the order", example = "550e8400-e29b-41d4-a716-446655440000")
        private UUID customerId;
        @NotEmpty
        @Valid
        @io.swagger.v3.oas.annotations.media.Schema(description = "List of products and quantities")
        private List<OrderItemRequest> items;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Optional promotion coupon code", example = "SAVE20")
        private String couponCode; // Optional
    }

    @Data
    @io.swagger.v3.oas.annotations.media.Schema(description = "Item in an order")
    public static class OrderItemRequest {
        @NotNull
        @io.swagger.v3.oas.annotations.media.Schema(description = "ID of the product", example = "550e8400-e29b-41d4-a716-446655441111")
        private UUID productId;
        @Min(1)
        @io.swagger.v3.oas.annotations.media.Schema(description = "Quantity to order", example = "2")
        private int quantity;
    }

    @Data
    @io.swagger.v3.oas.annotations.media.Schema(description = "Detailed order representation")
    public static class Response {
        @io.swagger.v3.oas.annotations.media.Schema(description = "Unique order ID", example = "550e8400-e29b-41d4-a716-446655442222")
        private UUID id;
        @io.swagger.v3.oas.annotations.media.Schema(description = "ID of the customer", example = "550e8400-e29b-41d4-a716-446655440000")
        private UUID customerId;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Current order status", example = "PENDING")
        private String status;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Total order amount", example = "599.98")
        private BigDecimal totalAmount;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Currency code", example = "USD")
        private String currency;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Order creation timestamp")
        private Instant createdAt;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Items included in the order")
        private List<OrderItemResponse> items;
    }

    @Data
    @io.swagger.v3.oas.annotations.media.Schema(description = "Item in an order response")
    public static class OrderItemResponse {
        @io.swagger.v3.oas.annotations.media.Schema(description = "ID of the product", example = "550e8400-e29b-41d4-a716-446655441111")
        private UUID productId;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Product SKU", example = "HDP-WRL-001")
        private String sku;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Quantity ordered", example = "2")
        private int quantity;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Unit price at time of order", example = "299.99")
        private BigDecimal unitPrice;
    }
}
