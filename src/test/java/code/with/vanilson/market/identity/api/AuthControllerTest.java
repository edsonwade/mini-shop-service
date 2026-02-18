package code.with.vanilson.market.identity.api;

import code.with.vanilson.market.identity.api.dto.AuthDto;
import code.with.vanilson.market.identity.application.AuthService;
import code.with.vanilson.market.identity.domain.UserRepository;
import code.with.vanilson.market.identity.infrastructure.CustomUserDetailsService;
import code.with.vanilson.market.identity.infrastructure.JwtAuthenticationFilter;
import code.with.vanilson.market.identity.infrastructure.JwtProvider;
import code.with.vanilson.market.shared.infrastructure.IdempotencyFilter;
import code.with.vanilson.market.shared.infrastructure.exception.GlobalExceptionHandler;
import code.with.vanilson.market.shared.infrastructure.exception.InvalidTwoFactorCodeException;
import code.with.vanilson.market.shared.infrastructure.exception.RefreshTokenExpiredException;
import code.with.vanilson.market.shared.infrastructure.exception.RefreshTokenNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@WithMockUser
@DisplayName("Auth Controller Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private IdempotencyFilter idempotencyFilter;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @Test
    @DisplayName("Should register user successfully")
    void shouldRegisterSuccessfully() throws Exception {
        // Given
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");
        request.setTenantId("tenant-1");

        AuthDto.RegisterResponse response = new AuthDto.RegisterResponse();
        response.setMessage("User registered");
        response.setRecoveryCodes(Set.of("code1", "code2"));

        when(authService.register(any(AuthDto.RegisterRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered"))
                .andExpect(jsonPath("$.recoveryCodes").isArray());

        verify(authService, times(1)).register(any());
    }

    @Test
    @DisplayName("Should login successfully")
    void shouldLoginSuccessfully() throws Exception {
        // Given
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");

        AuthDto.AuthResponse response = new AuthDto.AuthResponse("access", "refresh");

        when(authService.login(any(AuthDto.LoginRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh"));

        verify(authService, times(1)).login(any());
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void shouldRefreshTokenSuccessfully() throws Exception {
        // Given
        AuthDto.RefreshTokenRequest request = new AuthDto.RefreshTokenRequest();
        request.setRefreshToken("old-refresh");

        AuthDto.AuthResponse response = new AuthDto.AuthResponse("new-access", "new-refresh");

        when(authService.refreshToken(any(AuthDto.RefreshTokenRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/auth/refresh-token")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"));

        verify(authService, times(1)).refreshToken(any());
    }

    @Test
    @DisplayName("Should logout successfully")
    void shouldLogoutSuccessfully() throws Exception {
        // Given
        AuthDto.RefreshTokenRequest request = new AuthDto.RefreshTokenRequest();
        request.setRefreshToken("token");
        doNothing().when(authService).logout(any());

        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(authService, times(1)).logout(any());
    }

    @Test
    @DisplayName("Should return 400 when registration request is invalid")
    void shouldReturn400WhenRegistrationIsInvalid() throws Exception {
        // Given
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setEmail("invalid"); // Bad email

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("Should handle RefreshTokenNotFoundException")
    void shouldHandleRefreshTokenNotFoundException() throws Exception {
        // Given
        AuthDto.RefreshTokenRequest request = new AuthDto.RefreshTokenRequest();
        request.setRefreshToken("ghost");
        when(authService.refreshToken(any())).thenThrow(new RefreshTokenNotFoundException("Not found"));

        // When & Then
        mockMvc.perform(post("/api/auth/refresh-token")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("REFRESH_TOKEN_NOT_FOUND"));
    }

    @Test
    @DisplayName("Should handle RefreshTokenExpiredException")
    void shouldHandleRefreshTokenExpiredException() throws Exception {
        // Given
        AuthDto.RefreshTokenRequest request = new AuthDto.RefreshTokenRequest();
        request.setRefreshToken("expired");
        when(authService.refreshToken(any())).thenThrow(new RefreshTokenExpiredException("Expired"));

        // When & Then
        mockMvc.perform(post("/api/auth/refresh-token")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("REFRESH_TOKEN_EXPIRED"));
    }

    @Test
    @DisplayName("Should handle InvalidTwoFactorCodeException")
    void shouldHandleInvalidTwoFactorCodeException() throws Exception {
        // Given
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("pass");
        request.setTotpCode("000000");

        when(authService.login(any())).thenThrow(new InvalidTwoFactorCodeException("Invalid 2FA"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_2FA_CODE"));
    }
}
