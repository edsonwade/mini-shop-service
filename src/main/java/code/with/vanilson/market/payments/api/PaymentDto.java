package code.with.vanilson.market.payments.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentDto {

    @Data
    @io.swagger.v3.oas.annotations.media.Schema(description = "Request to process a payment")
    public static class ProcessRequest {
        @NotNull
        @io.swagger.v3.oas.annotations.media.Schema(description = "ID of the order to pay for", example = "550e8400-e29b-41d4-a716-446655442222")
        private UUID orderId;
        @DecimalMin("0.01")
        @io.swagger.v3.oas.annotations.media.Schema(description = "Payment amount", example = "599.98")
        private BigDecimal amount;
        @NotBlank
        @io.swagger.v3.oas.annotations.media.Schema(description = "Currency code", example = "USD")
        private String currency;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Payment method", example = "CREDIT_CARD")
        private String paymentMethod; // e.g., CREDIT_CARD
    }

    @Data
    @io.swagger.v3.oas.annotations.media.Schema(description = "Payment transaction response")
    public static class Response {
        @io.swagger.v3.oas.annotations.media.Schema(description = "Unique payment ID", example = "550e8400-e29b-41d4-a716-446655443333")
        private UUID id;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Associated order ID", example = "550e8400-e29b-41d4-a716-446655442222")
        private UUID orderId;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Paid amount", example = "599.98")
        private BigDecimal amount;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Transaction status", example = "COMPLETED")
        private String status;
    }
}
