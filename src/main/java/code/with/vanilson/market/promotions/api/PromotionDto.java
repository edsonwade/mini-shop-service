package code.with.vanilson.market.promotions.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

public class PromotionDto {

    @Data
    public static class CreateCouponRequest {
        @NotBlank
        private String code;
        @DecimalMin("0.01")
        private BigDecimal discountAmount;
        @NotBlank
        private String currency;
        @Future
        private Instant expiryDate;
    }

    @Data
    public static class Response {
        private String id;
        private String code;
        private BigDecimal discountAmount;
        private boolean active;
    }
}
