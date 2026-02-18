package code.with.vanilson.market.identity;

import code.with.vanilson.market.shared.infrastructure.test.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;



    @Test
    void register_login_refresh_logout_success_flow() throws Exception {
        // 1) Register
        String registerPayload = "{\n" +
                "  \"email\": \"john.doe@example.com\",\n" +
                "  \"password\": \"Secret123!\",\n" +
                "  \"tenantId\": \"tenant-1\"\n" +
                "}";

        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode reg = objectMapper.readTree(registerResponse);
        assertThat(reg.get("message").asText()).contains("User registered successfully");
        String recoveryCode = reg.get("recoveryCodes").elements().next().asText();

        // 2) Login with recovery code (bypasses TOTP)
        String loginPayload = objectMapper.createObjectNode()
                .put("email", "john.doe@example.com")
                .put("password", "Secret123!")
                .put("recoveryCode", recoveryCode)
                .toString();

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode login = objectMapper.readTree(loginResponse);
        String accessToken = login.get("accessToken").asText();
        String refreshToken = login.get("refreshToken").asText();
        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();

        // 3) Refresh token
        String refreshPayload = objectMapper.createObjectNode()
                .put("refreshToken", refreshToken)
                .toString();

        String refreshResponse = mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode refreshed = objectMapper.readTree(refreshResponse);
        assertThat(refreshed.get("accessToken").asText()).isNotBlank();
        assertThat(refreshed.get("refreshToken").asText()).isNotBlank();

        // 4) Logout (204)
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload))
                .andExpect(status().isNoContent());
    }

    @Test
    void login_invalid_2fa_uses_global_exception_handler() throws Exception {
        // Register user first
        String registerPayload = "{\n" +
                "  \"email\": \"jane.doe@example.com\",\n" +
                "  \"password\": \"Secret123!\",\n" +
                "  \"tenantId\": \"tenant-1\"\n" +
                "}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isOk());

        // Try login without providing totp or valid recovery code
        String loginPayload = objectMapper.createObjectNode()
                .put("email", "jane.doe@example.com")
                .put("password", "Secret123!")
                .put("totpCode", "000000") // invalid
                .toString();

        String errorBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        JsonNode error = objectMapper.readTree(errorBody);
        assertThat(error.get("status").asInt()).isEqualTo(401);
        assertThat(error.get("errorCode").asText()).isEqualTo("INVALID_2FA_CODE");
        assertThat(error.get("message").asText()).contains("Invalid 2FA Code");
    }
}
