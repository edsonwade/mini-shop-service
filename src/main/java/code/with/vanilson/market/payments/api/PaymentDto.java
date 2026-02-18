package code.with.vanilson.market.payments.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentDto {

    @Data
    public static class ProcessRequest {
        @NotBlank
        private UUID orderId;
        @DecimalMin("0.01")
        private BigDecimal amount;
        @NotBlank
        private String currency;
        private String paymentMethod; // e.g., CREDIT_CARD
    }

    @Data
    public static class Response {
        private UUID id;
        private UUID orderId;
        private BigDecimal amount;
        private String status;
    }
}
