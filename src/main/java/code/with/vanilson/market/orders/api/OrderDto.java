package code.with.vanilson.market.orders.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class OrderDto {

    @Data
    public static class CreateRequest {
        @NotBlank
        private String tenantId;
        @NotBlank
        private UUID customerId;
        @NotEmpty
        private List<OrderItemRequest> items;
        private String couponCode; // Optional
    }

    @Data
    public static class OrderItemRequest {
        @NotBlank
        private UUID productId;
        @Min(1)
        private int quantity;
    }

    @Data
    public static class Response {
        private UUID id;
        private UUID customerId;
        private String status;
        private BigDecimal totalAmount;
        private String currency;
        private Instant createdAt;
        private List<OrderItemResponse> items;
    }

    @Data
    public static class OrderItemResponse {
        private UUID productId;
        private String sku;
        private int quantity;
        private BigDecimal unitPrice;
    }
}
