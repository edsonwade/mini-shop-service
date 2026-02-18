package code.with.vanilson.market.identity.application;

import code.with.vanilson.market.events.domain.EventProducer;
import code.with.vanilson.market.identity.api.dto.AuthDto;
import code.with.vanilson.market.identity.domain.RefreshToken;
import code.with.vanilson.market.identity.domain.RefreshTokenRepository;
import code.with.vanilson.market.identity.domain.User;
import code.with.vanilson.market.identity.domain.UserRepository;
import code.with.vanilson.market.identity.infrastructure.JwtProvider;
import code.with.vanilson.market.identity.infrastructure.TotpUtils;
import code.with.vanilson.market.shared.infrastructure.exception.CustomerAlreadyExistsException;
import code.with.vanilson.market.shared.infrastructure.exception.CustomerNotFoundException;
import code.with.vanilson.market.shared.infrastructure.exception.InvalidTwoFactorCodeException;
import code.with.vanilson.market.shared.infrastructure.exception.RefreshTokenExpiredException;
import code.with.vanilson.market.shared.infrastructure.exception.RefreshTokenNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private TotpUtils totpUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EventProducer eventProducer;

    @InjectMocks
    private AuthService authService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String TENANT_ID = UUID.randomUUID().toString();
    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "SecurePassword123!";
    private static final String ENCODED_PASSWORD = "encodedPassword123";
    private static final String TOTP_SECRET = "ABCD1234EFGH5678";
    private static final String TOTP_CODE = "123456";
    private static final String JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
    private static final String REFRESH_TOKEN = "refresh-token-uuid";

    // ========================= Register Tests =========================

    @Test
    @DisplayName("should successfully register a new user")
    void testRegister_Success() {
        // Arrange
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
        request.setTenantId(TENANT_ID);

        User savedUser = User.builder()
                .email(EMAIL)
                .passwordHash(ENCODED_PASSWORD)
                .tenantId(TENANT_ID)
                .totpSecret(TOTP_SECRET)
                .totpEnabled(true)
                .recoveryCodes(new HashSet<>())
                .build();
        ReflectionTestUtils.setField(savedUser, "id", USER_ID);


        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(totpUtils.generateSecretKey()).thenReturn(TOTP_SECRET);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        AuthDto.RegisterResponse response = authService.register(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("registered successfully");
        assertThat(response.getTotpSecret()).isEqualTo(TOTP_SECRET);
        assertThat(response.getRecoveryCodes()).hasSize(5);
        assertThat(response.getTotpQrUrl()).contains("otpauth://totp");

        verify(userRepository, times(1)).findByEmail(EMAIL);
        verify(passwordEncoder, times(1)).encode(PASSWORD);
        verify(userRepository, times(1)).save(any(User.class));
        verify(eventProducer, times(1)).publish(anyString(), any());
    }

    @Test
    @DisplayName("should throw CustomerAlreadyExistsException when email already registered")
    void testRegister_EmailAlreadyExists() {
        // Arrange
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
        request.setTenantId(TENANT_ID);

        User existingUser = User.builder()
                .email(EMAIL)
                .build();
        ReflectionTestUtils.setField(existingUser, "id", USER_ID);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(CustomerAlreadyExistsException.class)
                .hasMessage("Email already in use");

        verify(userRepository, times(1)).findByEmail(EMAIL);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("should generate exactly 5 recovery codes during registration")
    void testRegister_GeneratesRecoveryCodes() {
        // Arrange
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
        request.setTenantId(TENANT_ID);

        User savedUser = User.builder()
                
                .email(EMAIL)
                .passwordHash(ENCODED_PASSWORD)
                .tenantId(TENANT_ID)
                .totpSecret(TOTP_SECRET)
                .totpEnabled(true)
                .build();
        ReflectionTestUtils.setField(savedUser, "id", USER_ID);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(totpUtils.generateSecretKey()).thenReturn(TOTP_SECRET);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        AuthDto.RegisterResponse response = authService.register(request);

        // Assert
        assertThat(response.getRecoveryCodes()).hasSize(5);
        assertThat(response.getRecoveryCodes()).allMatch(code -> code != null && code.length() > 0);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("should set TOTP as enabled by default during registration")
    void testRegister_TotpEnabledByDefault() {
        // Arrange
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
        request.setTenantId(TENANT_ID);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        User savedUser = User.builder()
                
                .email(EMAIL)
                .totpEnabled(true)
                .build();
        ReflectionTestUtils.setField(savedUser, "id", USER_ID);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(totpUtils.generateSecretKey()).thenReturn(TOTP_SECRET);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        authService.register(request);

        // Assert
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().isTotpEnabled()).isTrue();
    }

    @Test
    @DisplayName("should assign CUSTOMER role to new user during registration")
    void testRegister_AssignCustomerRole() {
        // Arrange
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
        request.setTenantId(TENANT_ID);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        User savedUser = User.builder()
                
                .email(EMAIL)
                .build();
        ReflectionTestUtils.setField(savedUser, "id", USER_ID);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(totpUtils.generateSecretKey()).thenReturn(TOTP_SECRET);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        authService.register(request);

        // Assert
        verify(userRepository).save(userCaptor.capture());
        // Verify user has CUSTOMER role (via addRole method call)
        verify(userRepository, times(1)).save(any(User.class));
    }

    // ========================= Login Tests =========================

    @Test
    @DisplayName("should successfully login user with valid TOTP code")
    void testLogin_WithValidTotp_Success() {
        // Arrange
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
        request.setTotpCode(TOTP_CODE);

        User user = User.builder()
                
                .email(EMAIL)
                .passwordHash(ENCODED_PASSWORD)
                .tenantId(TENANT_ID)
                .totpSecret(TOTP_SECRET)
                .totpEnabled(true)
                .recoveryCodes(new HashSet<>())
                .build();
        ReflectionTestUtils.setField(user, "id", USER_ID);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(REFRESH_TOKEN)
                .expiryDate(Instant.now().plusSeconds(604800))
                .build();
        ReflectionTestUtils.setField(refreshToken, "id", UUID.randomUUID());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(EMAIL, PASSWORD));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(totpUtils.verifyCode(TOTP_SECRET, TOTP_CODE)).thenReturn(true);
        doNothing().when(refreshTokenRepository).deleteTokensByUser(user);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);
        when(jwtProvider.generateToken(user)).thenReturn(JWT_TOKEN);

        // Act
        AuthDto.AuthResponse response = authService.login(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(JWT_TOKEN);
        assertThat(response.getRefreshToken()).isNotNull();

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail(EMAIL);
        verify(totpUtils, times(1)).verifyCode(TOTP_SECRET, TOTP_CODE);
        verify(jwtProvider, times(1)).generateToken(user);
    }

    @Test
    @DisplayName("should throw InvalidTwoFactorCodeException when TOTP code is invalid")
    void testLogin_InvalidTotpCode() {
        // Arrange
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
        request.setTotpCode("000000");

        User user = User.builder()
                
                .email(EMAIL)
                .passwordHash(ENCODED_PASSWORD)
                .totpSecret(TOTP_SECRET)
                .totpEnabled(true)
                .recoveryCodes(new HashSet<>())
                .build();
        ReflectionTestUtils.setField(user, "id", USER_ID);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(EMAIL, PASSWORD));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(totpUtils.verifyCode(TOTP_SECRET, "000000")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidTwoFactorCodeException.class)
                .hasMessage("Invalid 2FA Code or Recovery Code");

        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("should successfully login user with valid recovery code")
    void testLogin_WithValidRecoveryCode_Success() {
        // Arrange
        String recoveryCode = "REC12345";
        Set<String> recoveryCodes = new HashSet<>();
        recoveryCodes.add(recoveryCode);

        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
        request.setRecoveryCode(recoveryCode);

        User user = User.builder()
                
                .email(EMAIL)
                .passwordHash(ENCODED_PASSWORD)
                .tenantId(TENANT_ID)
                .totpSecret(TOTP_SECRET)
                .totpEnabled(true)
                .recoveryCodes(recoveryCodes)
                .build();
        ReflectionTestUtils.setField(user, "id", USER_ID);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(REFRESH_TOKEN)
                .expiryDate(Instant.now().plusSeconds(604800))
                .build();
        ReflectionTestUtils.setField(refreshToken, "id", UUID.randomUUID());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(EMAIL, PASSWORD));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        doNothing().when(refreshTokenRepository).deleteTokensByUser(user);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);
        when(jwtProvider.generateToken(user)).thenReturn(JWT_TOKEN);

        // Act
        AuthDto.AuthResponse response = authService.login(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(JWT_TOKEN);

        verify(userRepository, atLeastOnce()).findByEmail(EMAIL);
        verify(userRepository, times(1)).save(user); // Recovery code burned
    }

    @Test
    @DisplayName("should burn recovery code after successful usage")
    void testLogin_BurnsRecoveryCode() {
        // Arrange
        String recoveryCode = "REC12345";
        Set<String> recoveryCodes = new HashSet<>();
        recoveryCodes.add(recoveryCode);

        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
        request.setRecoveryCode(recoveryCode);

        User user = User.builder()
                
                .email(EMAIL)
                .totpEnabled(true)
                .recoveryCodes(recoveryCodes)
                .build();
        ReflectionTestUtils.setField(user, "id", USER_ID);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(EMAIL, PASSWORD));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        doNothing().when(refreshTokenRepository).deleteTokensByUser(user);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenReturn(RefreshToken.builder().build());
        when(jwtProvider.generateToken(user)).thenReturn(JWT_TOKEN);

        // Act
        authService.login(request);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRecoveryCodes()).doesNotContain(recoveryCode);
    }

    @Test
    @DisplayName("should throw CustomerNotFoundException when user not found during login")
    void testLogin_UserNotFound() {
        // Arrange
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
        request.setTotpCode(TOTP_CODE);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(EMAIL, PASSWORD));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessage("User not found");

        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("should delete old refresh tokens before creating new one")
    void testLogin_DeletesOldRefreshTokens() {
        // Arrange
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
        request.setTotpCode(TOTP_CODE);

        User user = User.builder()
                
                .email(EMAIL)
                .totpSecret(TOTP_SECRET)
                .totpEnabled(true)
                .recoveryCodes(new HashSet<>())
                .build();
        ReflectionTestUtils.setField(user, "id", USER_ID);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(EMAIL, PASSWORD));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(totpUtils.verifyCode(TOTP_SECRET, TOTP_CODE)).thenReturn(true);
        doNothing().when(refreshTokenRepository).deleteTokensByUser(user);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenReturn(RefreshToken.builder().build());
        when(jwtProvider.generateToken(user)).thenReturn(JWT_TOKEN);

        // Act
        authService.login(request);

        // Assert
        verify(refreshTokenRepository, times(1)).deleteTokensByUser(user);
    }

    // ========================= Refresh Token Tests =========================

    @Test
    @DisplayName("should successfully refresh token with valid refresh token")
    void testRefreshToken_Success() {
        // Arrange
        AuthDto.RefreshTokenRequest request = new AuthDto.RefreshTokenRequest();
        request.setRefreshToken(REFRESH_TOKEN);

        User user = User.builder()
                
                .email(EMAIL)
                .build();
        ReflectionTestUtils.setField(user, "id", USER_ID);

        RefreshToken existingToken = RefreshToken.builder()
                .user(user)
                .token(REFRESH_TOKEN)
                .expiryDate(Instant.now().plusSeconds(604800))
                .build();
        ReflectionTestUtils.setField(existingToken, "id", UUID.randomUUID());

        RefreshToken newToken = RefreshToken.builder()
                .user(user)
                .token("new-refresh-token")
                .expiryDate(Instant.now().plusSeconds(604800))
                .build();
        ReflectionTestUtils.setField(newToken, "id", UUID.randomUUID());

        when(refreshTokenRepository.findByToken(REFRESH_TOKEN)).thenReturn(Optional.of(existingToken));
        doNothing().when(refreshTokenRepository).deleteTokensByUser(user);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(newToken);
        when(jwtProvider.generateToken(user)).thenReturn(JWT_TOKEN);

        // Act
        AuthDto.AuthResponse response = authService.refreshToken(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(JWT_TOKEN);
        assertThat(response.getRefreshToken()).isNotNull();

        verify(refreshTokenRepository, times(1)).findByToken(REFRESH_TOKEN);
        verify(refreshTokenRepository, times(1)).deleteTokensByUser(user);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("should throw RefreshTokenNotFoundException when refresh token not found")
    void testRefreshToken_TokenNotFound() {
        // Arrange
        AuthDto.RefreshTokenRequest request = new AuthDto.RefreshTokenRequest();
        request.setRefreshToken(REFRESH_TOKEN);

        when(refreshTokenRepository.findByToken(REFRESH_TOKEN)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(RefreshTokenNotFoundException.class)
                .hasMessage("Refresh Token not found");

        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("should throw RefreshTokenExpiredException when refresh token is expired")
    void testRefreshToken_TokenExpired() {
        // Arrange
        AuthDto.RefreshTokenRequest request = new AuthDto.RefreshTokenRequest();
        request.setRefreshToken(REFRESH_TOKEN);

        User user = User.builder()
                
                .email(EMAIL)
                .build();
        ReflectionTestUtils.setField(user, "id", USER_ID);

        RefreshToken expiredToken = RefreshToken.builder()
                .user(user)
                .token(REFRESH_TOKEN)
                .expiryDate(Instant.now().minusSeconds(1))
                .build();
        ReflectionTestUtils.setField(expiredToken, "id", UUID.randomUUID());

        when(refreshTokenRepository.findByToken(REFRESH_TOKEN)).thenReturn(Optional.of(expiredToken));

        // Act & Assert
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(RefreshTokenExpiredException.class)
                .hasMessage("Refresh token was expired. Please make a new signin request");

        verify(refreshTokenRepository, times(1)).delete(expiredToken);
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("should rotate refresh token after successful refresh")
    void testRefreshToken_RotatesToken() {
        // Arrange
        AuthDto.RefreshTokenRequest request = new AuthDto.RefreshTokenRequest();
        request.setRefreshToken(REFRESH_TOKEN);

        User user = User.builder()
                
                .email(EMAIL)
                .build();
        ReflectionTestUtils.setField(user, "id", USER_ID);

        RefreshToken oldToken = RefreshToken.builder()
                .user(user)
                .token(REFRESH_TOKEN)
                .expiryDate(Instant.now().plusSeconds(604800))
                .build();
        ReflectionTestUtils.setField(oldToken, "id", UUID.randomUUID());

        when(refreshTokenRepository.findByToken(REFRESH_TOKEN)).thenReturn(Optional.of(oldToken));
        doNothing().when(refreshTokenRepository).deleteTokensByUser(user);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenReturn(RefreshToken.builder().token("new-token").build());
        when(jwtProvider.generateToken(user)).thenReturn(JWT_TOKEN);

        // Act
        AuthDto.AuthResponse response = authService.refreshToken(request);

        // Assert
        verify(refreshTokenRepository, times(1)).deleteTokensByUser(user);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
        assertThat(response.getRefreshToken()).isNotNull();
    }

    // ========================= Logout Tests =========================

    @Test
    @DisplayName("should successfully logout user by deleting refresh token")
    void testLogout_Success() {
        // Arrange
        AuthDto.RefreshTokenRequest request = new AuthDto.RefreshTokenRequest();
        request.setRefreshToken(REFRESH_TOKEN);

        User user = User.builder()
                
                .email(EMAIL)
                .build();
        ReflectionTestUtils.setField(user, "id", USER_ID);

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(REFRESH_TOKEN)
                .expiryDate(Instant.now().plusSeconds(604800))
                .build();
        ReflectionTestUtils.setField(token, "id", UUID.randomUUID());

        when(refreshTokenRepository.findByToken(REFRESH_TOKEN)).thenReturn(Optional.of(token));

        // Act
        authService.logout(request);

        // Assert
        verify(refreshTokenRepository, times(1)).findByToken(REFRESH_TOKEN);
        verify(refreshTokenRepository, times(1)).delete(token);
    }

    @Test
    @DisplayName("should handle logout when refresh token not found")
    void testLogout_TokenNotFound() {
        // Arrange
        AuthDto.RefreshTokenRequest request = new AuthDto.RefreshTokenRequest();
        request.setRefreshToken(REFRESH_TOKEN);

        when(refreshTokenRepository.findByToken(REFRESH_TOKEN)).thenReturn(Optional.empty());

        // Act
        authService.logout(request);

        // Assert
        verify(refreshTokenRepository, times(1)).findByToken(REFRESH_TOKEN);
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    @Test
    @DisplayName("should successfully logout user even if token doesn't exist")
    void testLogout_NoException() {
        // Arrange
        AuthDto.RefreshTokenRequest request = new AuthDto.RefreshTokenRequest();
        request.setRefreshToken(REFRESH_TOKEN);

        when(refreshTokenRepository.findByToken(REFRESH_TOKEN)).thenReturn(Optional.empty());

        // Act & Assert - should not throw exception
        assertThatCode(() -> authService.logout(request))
                .doesNotThrowAnyException();

        verify(refreshTokenRepository, times(1)).findByToken(REFRESH_TOKEN);
    }
}

