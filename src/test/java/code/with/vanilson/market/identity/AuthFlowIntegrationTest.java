package code.with.vanilson.market.identity;

import code.with.vanilson.market.identity.api.dto.AuthDto;
import code.with.vanilson.market.identity.domain.RefreshTokenRepository;
import code.with.vanilson.market.identity.domain.UserRepository;
import code.with.vanilson.market.shared.infrastructure.test.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AuthFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should Register, Login with Recovery Code, Refresh Token, and Logout")
    void shouldRegisterLoginRefreshTokenAndLogout() throws Exception {
        // 1. Register
        AuthDto.RegisterRequest registerRequest = new AuthDto.RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setTenantId("tenant-1");

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String registerResponseStr = registerResult.getResponse().getContentAsString();
        AuthDto.RegisterResponse registerResponse = objectMapper.readValue(registerResponseStr,
                AuthDto.RegisterResponse.class);
        Set<String> recoveryCodes = registerResponse.getRecoveryCodes();
        assertFalse(recoveryCodes.isEmpty(), "Recovery codes should not be empty");
        String recoveryCode = recoveryCodes.iterator().next();

        // 2. Login using Recovery Code
        AuthDto.LoginRequest loginRequest = new AuthDto.LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
        loginRequest.setRecoveryCode(recoveryCode);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        String loginResponseStr = loginResult.getResponse().getContentAsString();
        AuthDto.AuthResponse authResponse = objectMapper.readValue(loginResponseStr, AuthDto.AuthResponse.class);
        String refreshToken = authResponse.getRefreshToken();
        String accessToken = authResponse.getAccessToken();

        assertNotNull(refreshToken);
        assertNotNull(accessToken);

        // 3. Refresh Token
        AuthDto.RefreshTokenRequest refreshRequest = new AuthDto.RefreshTokenRequest();
        refreshRequest.setRefreshToken(refreshToken);

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        String refreshResponseStr = refreshResult.getResponse().getContentAsString();
        AuthDto.AuthResponse refreshResponse = objectMapper.readValue(refreshResponseStr, AuthDto.AuthResponse.class);
        String newRefreshToken = refreshResponse.getRefreshToken();
        String newAccessToken = refreshResponse.getAccessToken();

        assertNotNull(newRefreshToken, "New refresh token should not be null");
        assertNotNull(newAccessToken, "New access token should not be null");
        assertNotEquals(refreshToken, newRefreshToken, "Refresh token should be rotated");

        // 4. Logout (using new Refresh Token)
        AuthDto.RefreshTokenRequest logoutRequest = new AuthDto.RefreshTokenRequest();
        logoutRequest.setRefreshToken(newRefreshToken);

        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isNoContent());

        // 5. Try Refresh Token again (Should fail)
        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isNotFound()); // Or 404/400 depending on exception handling.

    }
}
