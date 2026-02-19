package code.with.vanilson.market.promotions.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

public class PromotionDto {

    @Data
    @io.swagger.v3.oas.annotations.media.Schema(description = "Request to create a new promotion coupon")
    public static class CreateCouponRequest {
        @NotBlank
        @io.swagger.v3.oas.annotations.media.Schema(description = "Unique coupon code", example = "SAVE20")
        private String code;
        @DecimalMin("0.01")
        @io.swagger.v3.oas.annotations.media.Schema(description = "Discount value", example = "20.00")
        private BigDecimal discountAmount;
        @NotBlank
        @io.swagger.v3.oas.annotations.media.Schema(description = "Currency code", example = "USD")
        private String currency;
        @Future
        @io.swagger.v3.oas.annotations.media.Schema(description = "Coupon expiration timestamp")
        private Instant expiryDate;
    }

    @Data
    @io.swagger.v3.oas.annotations.media.Schema(description = "Promotion coupon representation")
    public static class Response {
        @io.swagger.v3.oas.annotations.media.Schema(description = "Unique absolute coupon ID", example = "550e8400-e29b-41d4-a716-446655444444")
        private String id;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Coupon code", example = "SAVE20")
        private String code;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Discount amount", example = "20.00")
        private BigDecimal discountAmount;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Whether the coupon is still active", example = "true")
        private boolean active;
    }
}
