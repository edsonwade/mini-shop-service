package code.with.vanilson.market.products.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

public class ProductDto {

    @Data
    @io.swagger.v3.oas.annotations.media.Schema(description = "Request to create a new product")
    public static class CreateRequest {
        @NotBlank
        @io.swagger.v3.oas.annotations.media.Schema(description = "Tenant ID for multi-tenant data isolation", example = "tenant-001")
        private String tenantId;
        @NotBlank
        @io.swagger.v3.oas.annotations.media.Schema(description = "Product display name", example = "Premium Wireless Headphones")
        private String name;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Detailed product description", example = "Noise-cancelling over-ear headphones with 40h battery life")
        private String description;
        @NotBlank
        @io.swagger.v3.oas.annotations.media.Schema(description = "Unique Stock Keeping Unit identifier", example = "HDP-WRL-001")
        private String sku;
        @DecimalMin("0.01")
        @io.swagger.v3.oas.annotations.media.Schema(description = "Product unit price", example = "299.99")
        private BigDecimal price;
        @NotBlank
        @io.swagger.v3.oas.annotations.media.Schema(description = "Currency code (ISO 4217)", example = "USD")
        private String currency;
        @Min(0)
        @io.swagger.v3.oas.annotations.media.Schema(description = "Initial stock level", example = "100")
        private int inventoryCount;
    }

    @Data
    @io.swagger.v3.oas.annotations.media.Schema(description = "Product resource representation")
    public static class Response {
        @io.swagger.v3.oas.annotations.media.Schema(description = "Unique absolute product ID", example = "550e8400-e29b-41d4-a716-446655441111")
        private String id;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Product name", example = "Premium Wireless Headphones")
        private String name;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Product SKU", example = "HDP-WRL-001")
        private String sku;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Product price", example = "299.99")
        private BigDecimal price;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Currency code", example = "USD")
        private String currency;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Available inventory in stock", example = "100")
        private int inventoryCount;
    }
}
