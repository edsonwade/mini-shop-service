package code.with.vanilson.market.customers;

import code.with.vanilson.market.shared.infrastructure.test.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class CustomerControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String bearerToken;

    @BeforeEach
    void setUp() throws Exception {
        String testEmail = "test.customer+" + java.util.UUID.randomUUID() + "@example.com";
        // 1. Register user
        String registerPayload = "{\n" +
                "  \"email\": \"" + testEmail + "\",\n" +
                "  \"password\": \"Secret123!\",\n" +
                "  \"tenantId\": \"tenant-1\"\n" +
                "}";

        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode reg = objectMapper.readTree(registerResponse);
        String recoveryCode = reg.get("recoveryCodes").elements().next().asText();

        // 2. Login with recovery code
        String loginPayload = objectMapper.createObjectNode()
                .put("email", testEmail)
                .put("password", "Secret123!")
                .put("recoveryCode", recoveryCode)
                .toString();

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode login = objectMapper.readTree(loginResponse);
        this.bearerToken = "Bearer " + login.get("accessToken").asText();
    }

    @Test
    void create_get_update_delete_customer_success_flow() throws Exception {
        // Create
        String createPayload = "{\n" +
                "  \"tenantId\": \"tenant-1\",\n" +
                "  \"name\": \"Alice\",\n" +
                "  \"email\": \"alice@example.com\",\n" +
                "  \"phone\": \"+1000000000\"\n" +
                "}";

        String created = mockMvc.perform(post("/api/customers")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode createdJson = objectMapper.readTree(created);
        String id = createdJson.get("id").asText();
        assertThat(id).isNotBlank();
        assertThat(createdJson.get("name").asText()).isEqualTo("Alice");

        // Get by id
        mockMvc.perform(get("/api/customers/" + id)
                        .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@example.com"));

        // Update
        String updatePayload = "{\n" +
                "  \"name\": \"Alice Updated\",\n" +
                "  \"email\": \"alice.updated@example.com\",\n" +
                "  \"phone\": \"+12223334444\",\n" +
                "  \"kycVerified\": true\n" +
                "}";

        String updated = mockMvc.perform(put("/api/customers/" + id)
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode updatedJson = objectMapper.readTree(updated);
        assertThat(updatedJson.get("name").asText()).isEqualTo("Alice Updated");
        assertThat(updatedJson.get("kycVerified").asBoolean()).isTrue();

        // Delete (204)
        mockMvc.perform(delete("/api/customers/" + id)
                        .header("Authorization", bearerToken))
                .andExpect(status().isNoContent());

        // Now GET should return 404 with GlobalExceptionHandler response
        String error = mockMvc.perform(get("/api/customers/" + id)
                        .header("Authorization", bearerToken))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        JsonNode err = objectMapper.readTree(error);
        assertThat(err.get("status").asInt()).isEqualTo(404);
        assertThat(err.get("errorCode").asText()).isEqualTo("CUSTOMER_NOT_FOUND");
    }

    @Test
    void create_customer_conflict_uses_global_exception_handler() throws Exception {
        String payload = "{\n" +
                "  \"tenantId\": \"tenant-1\",\n" +
                "  \"name\": \"Bob\",\n" +
                "  \"email\": \"bob@example.com\"\n" +
                "}";

        mockMvc.perform(post("/api/customers")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        // Try to create again -> should be conflict
        String error = mockMvc.perform(post("/api/customers")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andReturn().getResponse().getContentAsString();

        JsonNode err = objectMapper.readTree(error);
        assertThat(err.get("status").asInt()).isEqualTo(409);
        assertThat(err.get("errorCode").asText()).isEqualTo("CUSTOMER_ALREADY_EXISTS");
    }

    @Test
    void get_nonexistent_customer_returns_404() throws Exception {
        mockMvc.perform(get("/api/customers/" + UUID.randomUUID())
                        .header("Authorization", bearerToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_NOT_FOUND"));
    }
}
