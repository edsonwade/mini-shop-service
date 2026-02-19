package code.with.vanilson.market.identity.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class AuthDto {

    @Data
    @io.swagger.v3.oas.annotations.media.Schema(description = "Request for user login")
    public static class LoginRequest {
        @NotBlank
        @Email
        @io.swagger.v3.oas.annotations.media.Schema(description = "User email address", example = "admin@example.com")
        private String email;
        @NotBlank
        @io.swagger.v3.oas.annotations.media.Schema(description = "User password", example = "P@ssword123")
        private String password;
        @io.swagger.v3.oas.annotations.media.Schema(description = "2FA TOTP code (if enabled)", example = "123456")
        private String totpCode;
        @io.swagger.v3.oas.annotations.media.Schema(description = "2FA recovery code", example = "REC-123-456")
        private String recoveryCode;
    }

    @Data
    @io.swagger.v3.oas.annotations.media.Schema(description = "Request for new user registration")
    public static class RegisterRequest {
        @NotBlank
        @Email
        @io.swagger.v3.oas.annotations.media.Schema(description = "User email address", example = "newuser@example.com")
        private String email;
        @NotBlank
        @io.swagger.v3.oas.annotations.media.Schema(description = "Secure password", example = "SecureP@ss123")
        private String password;
        @NotBlank
        @io.swagger.v3.oas.annotations.media.Schema(description = "Tenant identifier for multi-tenant isolation", example = "tenant-001")
        private String tenantId;
    }

    @Data
    @io.swagger.v3.oas.annotations.media.Schema(description = "Response containing JWT tokens")
    public static class AuthResponse {
        @io.swagger.v3.oas.annotations.media.Schema(description = "JWT Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        private String accessToken;
        @io.swagger.v3.oas.annotations.media.Schema(description = "JWT Refresh Token", example = "def-456-ghi...")
        private String refreshToken;
        @io.swagger.v3.oas.annotations.media.Schema(description = "Token type", example = "Bearer")
        private String tokenType = "Bearer";

        public AuthResponse(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }

    @Data
    @io.swagger.v3.oas.annotations.media.Schema(description = "Response after successful registration")
    public static class RegisterResponse {
        @io.swagger.v3.oas.annotations.media.Schema(description = "Success message", example = "User registered successfully")
        private String message;
        @io.swagger.v3.oas.annotations.media.Schema(description = "TOTP secret for 2FA setup", example = "JBSWY3DPEHPK3PXP")
        private String totpSecret;
        @io.swagger.v3.oas.annotations.media.Schema(description = "URL for QR code setup", example = "otpauth://totp/MiniMarket...")
        private String totpQrUrl; // Optional
        @io.swagger.v3.oas.annotations.media.Schema(description = "One-time recovery codes")
        private java.util.Set<String> recoveryCodes;
    }

    @Data
    @io.swagger.v3.oas.annotations.media.Schema(description = "Request to rotate access token")
    public static class RefreshTokenRequest {
        @NotBlank
        @io.swagger.v3.oas.annotations.media.Schema(description = "Valid refresh token", example = "def-456-ghi...")
        private String refreshToken;
    }
}
