package code.with.vanilson.market.identity.api;

import code.with.vanilson.market.identity.api.dto.AuthDto;
import code.with.vanilson.market.identity.application.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Identity & Access Management", description = "Endpoints for authentication, registration, and token management")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register new user", description = "Creates a new user account with multi-tenant isolation and generates 2FA credentials.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid registration data or email already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthDto.RegisterResponse> register(@RequestBody @Valid AuthDto.RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "User login", description = "Authenticates user credentials and returns JWT access and refresh tokens.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "403", description = "Account locked or 2FA required")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthDto.AuthResponse> login(@RequestBody @Valid AuthDto.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Refresh access token", description = "Rotates the access token using a valid refresh token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthDto.AuthResponse> refreshToken(@RequestBody @Valid AuthDto.RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @Operation(summary = "User logout", description = "Invalidates the refresh token and ends the session.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Logged out successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid logout request")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid AuthDto.RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }
}
