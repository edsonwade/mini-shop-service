package code.with.vanilson.market.customers.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class CustomerDto {

    @Data
    @io.swagger.v3.oas.annotations.media.Schema(description = "Request to create a new customer")
    public static class CreateRequest {
        @NotBlank
        @io.swagger.v3.oas.annotations.media.Schema(description = "Tenant ID for data isolation", example = "tenant-001")
        private String tenantId;
        @NotBlank
        @io.swagger.v3.oas.annotations.media.Schema(description = "Customer full name", example = "John Doe")
        private String name;
        @NotBlank
        @Email
        @io.swagger.v3.oas.annotations.media.Schema(description = "Customer email address", example = "john.doe@example.com")
        private String email;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Customer phone number", example = "+1234567890")
        private String phone;
    }

    @Data
    @io.swagger.v3.oas.annotations.media.Schema(description = "Request to update an existing customer")
    public static class UpdateRequest {
        @NotBlank
        @io.swagger.v3.oas.annotations.media.Schema(description = "Updated customer name", example = "John Updated Doe")
        private String name;

        @NotBlank
        @Email
        @io.swagger.v3.oas.annotations.media.Schema(description = "Updated customer email", example = "john.updated@example.com")
        private String email;

        @io.swagger.v3.oas.annotations.media.Schema(description = "Updated phone number", example = "+0987654321")
        private String phone;

        @io.swagger.v3.oas.annotations.media.Schema(description = "KYC verification status", example = "true")
        private boolean kycVerified;
    }

    @Data
    @io.swagger.v3.oas.annotations.media.Schema(description = "Customer resource representation")
    public static class Response implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Unique customer ID", example = "550e8400-e29b-41d4-a716-446655440000")
        private String id;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Customer name", example = "John Doe")
        private String name;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Customer email", example = "john.doe@example.com")
        private String email;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Whether KYC is verified", example = "false")
        private boolean kycVerified;
    }
}
