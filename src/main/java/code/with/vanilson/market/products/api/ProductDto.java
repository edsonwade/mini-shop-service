package code.with.vanilson.market.products.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

public class ProductDto {

    @Data
    public static class CreateRequest {
        @NotBlank
        private String tenantId;
        @NotBlank
        private String name;
        private String description;
        @NotBlank
        private String sku;
        @DecimalMin("0.01")
        private BigDecimal price;
        @NotBlank
        private String currency;
        @Min(0)
        private int inventoryCount;
    }

    @Data
    public static class Response {
        private String id;
        private String name;
        private String sku;
        private BigDecimal price;
        private String currency;
        private int inventoryCount;
    }
}
