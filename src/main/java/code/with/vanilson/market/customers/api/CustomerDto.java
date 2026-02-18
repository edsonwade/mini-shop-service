package code.with.vanilson.market.customers.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class CustomerDto {

    @Data
    public static class CreateRequest {
        @NotBlank
        private String tenantId;
        @NotBlank
        private String name;
        @NotBlank
        @Email
        private String email;
        private String phone;
    }

    @Data
    public static class UpdateRequest {
        @NotBlank
        private String name;

        @NotBlank
        @Email
        private String email;

        private String phone;

        private boolean kycVerified;
    }

    @Data
    public static class Response implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private String id;
        private String name;
        private String email;
        private boolean kycVerified;
    }
}
