package code.with.vanilson.market.identity.application;

import code.with.vanilson.market.events.domain.EventProducer;
import code.with.vanilson.market.identity.api.dto.AuthDto;
import code.with.vanilson.market.identity.domain.RefreshToken;
import code.with.vanilson.market.identity.domain.RefreshTokenRepository;
import code.with.vanilson.market.identity.domain.Role;
import code.with.vanilson.market.identity.domain.User;
import code.with.vanilson.market.identity.domain.UserRepository;
import code.with.vanilson.market.identity.infrastructure.JwtProvider;
import code.with.vanilson.market.identity.infrastructure.TotpUtils;
import code.with.vanilson.market.shared.domain.UserRegisteredEvent;
import code.with.vanilson.market.shared.infrastructure.exception.CustomerAlreadyExistsException;
import code.with.vanilson.market.shared.infrastructure.exception.CustomerNotFoundException;
import code.with.vanilson.market.shared.infrastructure.exception.InvalidTwoFactorCodeException;
import code.with.vanilson.market.shared.infrastructure.exception.RefreshTokenExpiredException;
import code.with.vanilson.market.shared.infrastructure.exception.RefreshTokenNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final TotpUtils totpUtils;
    private final AuthenticationManager authenticationManager;
    private final EventProducer eventProducer;

    @Transactional
    public AuthDto.RegisterResponse register(AuthDto.RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomerAlreadyExistsException("Email already in use");
        }

        String totpSecret = totpUtils.generateSecretKey();
        java.util.Set<String> recoveryCodes = new java.util.HashSet<>();
        // Generate 5 recovery codes
        for (int i = 0; i < 5; i++) {
            recoveryCodes.add(UUID.randomUUID().toString().substring(0, 8));
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .tenantId(request.getTenantId())
                .totpSecret(totpSecret)
                .totpEnabled(true) // Enforced by requirements
                .recoveryCodes(recoveryCodes)
                .build();

        user.addRole(Role.CUSTOMER); // Default role
        userRepository.save(user);

        AuthDto.RegisterResponse response = new AuthDto.RegisterResponse();
        response.setMessage("User registered successfully. Please setup your 2FA.");
        response.setTotpSecret(totpSecret);
        response.setTotpQrUrl(String.format("otpauth://totp/MiniMarket:%s?secret=%s&issuer=MiniMarket",
                request.getEmail(), totpSecret));
        response.setRecoveryCodes(recoveryCodes);

        // Publish Event for Async Customer Creation
        eventProducer.publish("user.registered", UserRegisteredEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .tenantId(user.getTenantId())
                .name(request.getEmail().split("@")[0]) // Simple heuristic for name
                .build());

        return response;
    }

    @Transactional
    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        // 1. Authenticate username/password first
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomerNotFoundException("User not found"));

        // 2. Verify TOTP or Recovery Code
        if (user.isTotpEnabled()) {
            boolean verified = false;
            // Check TOTP if provided
            if (request.getTotpCode() != null && !request.getTotpCode().isBlank()) {
                if (totpUtils.verifyCode(user.getTotpSecret(), request.getTotpCode())) {
                    verified = true;
                }
            }

            // Check Recovery Code if not verified yet
            if (!verified && request.getRecoveryCode() != null && !request.getRecoveryCode().isBlank()) {
                if (user.getRecoveryCodes().contains(request.getRecoveryCode())) {
                    user.getRecoveryCodes().remove(request.getRecoveryCode()); // Burn used code
                    userRepository.save(user);
                    verified = true;
                }
            }

            if (!verified) {
                throw new InvalidTwoFactorCodeException("Invalid 2FA Code or Recovery Code");
            }
        }

        // 3. Generate Tokens
        String jwt = jwtProvider.generateToken(user);

        // Rotate Refresh Token - delete old one first using query to bypass session cache
        refreshTokenRepository.deleteTokensByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusSeconds(60 * 60 * 24 * 7)) // 7 days
                .build();
        refreshTokenRepository.save(refreshToken);

        return new AuthDto.AuthResponse(jwt, refreshToken.getToken());
    }

    @Transactional
    public AuthDto.AuthResponse refreshToken(AuthDto.RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RefreshTokenNotFoundException("Refresh Token not found"));

        if (refreshToken.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(refreshToken);
            throw new RefreshTokenExpiredException("Refresh token was expired. Please make a new signin request");
        }

        User user = refreshToken.getUser();

        // Rotate Refresh Token - use query-based delete to bypass session cache
        refreshTokenRepository.deleteTokensByUser(user);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusSeconds(60 * 60 * 24 * 7)) // 7 days
                .build();
        refreshTokenRepository.save(newRefreshToken);

        String jwt = jwtProvider.generateToken(user);

        return new AuthDto.AuthResponse(jwt, newRefreshToken.getToken());
    }

    @Transactional
    public void logout(AuthDto.RefreshTokenRequest request) {
        refreshTokenRepository.findByToken(request.getRefreshToken())
                .ifPresent(refreshTokenRepository::delete);
    }
}
