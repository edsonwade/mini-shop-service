package code.with.vanilson.market.shared.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public class PaymentEvents {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentCapturedEvent implements Serializable {
        private UUID paymentId;
        private UUID orderId;
        private BigDecimal amount;
        private String currency;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentFailedEvent implements Serializable {
        private UUID paymentId;
        private UUID orderId;
        private String reason;
    }
}
