package code.with.vanilson.market.customers.api;

import code.with.vanilson.market.customers.application.CustomerService;
import code.with.vanilson.market.identity.domain.UserRepository;
import code.with.vanilson.market.identity.infrastructure.CustomUserDetailsService;
import code.with.vanilson.market.identity.infrastructure.JwtAuthenticationFilter;
import code.with.vanilson.market.identity.infrastructure.JwtProvider;
import code.with.vanilson.market.shared.domain.DomainException;
import code.with.vanilson.market.shared.infrastructure.IdempotencyFilter;
import org.springframework.data.redis.core.StringRedisTemplate;
import code.with.vanilson.market.shared.infrastructure.exception.CustomerAlreadyExistsException;
import code.with.vanilson.market.shared.infrastructure.exception.CustomerNotFoundException;
import code.with.vanilson.market.shared.infrastructure.exception.GlobalExceptionHandler;
import code.with.vanilson.market.shared.infrastructure.exception.KycAlreadyVerifiedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CustomerController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@WithMockUser
@DisplayName("Customer Controller Tests")
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

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
    @DisplayName("Should return all customers successfully")
    void shouldReturnAllCustomersSuccessfully() throws Exception {
        // Given
        CustomerDto.Response response = new CustomerDto.Response();
        response.setId(UUID.randomUUID().toString());
        response.setName("John Doe");
        response.setEmail("john.doe@example.com");
        response.setKycVerified(false);

        when(customerService.getAllcustomers()).thenReturn(List.of(response));

        // When & Then
        mockMvc.perform(get("/api/customers").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"));

        verify(customerService, times(1)).getAllcustomers();
    }

    @Test
    @DisplayName("Should return customer by ID successfully")
    void shouldReturnCustomerByIdSuccessfully() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        CustomerDto.Response response = new CustomerDto.Response();
        response.setId(id.toString());
        response.setName("John Doe");

        when(customerService.getCustomerById(id)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/customers/{id}", id).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(customerService, times(1)).getCustomerById(id);
    }

    @Test
    @DisplayName("Should create customer successfully")
    void shouldCreateCustomerSuccessfully() throws Exception {
        // Given
        CustomerDto.CreateRequest request = new CustomerDto.CreateRequest();
        request.setTenantId("tenant-1");
        request.setName("New Customer");
        request.setEmail("new@example.com");

        CustomerDto.Response response = new CustomerDto.Response();
        response.setId(UUID.randomUUID().toString());
        response.setName("New Customer");

        when(customerService.createCustomer(any(CustomerDto.CreateRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/customers")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Customer"));

        verify(customerService, times(1)).createCustomer(any());
    }

    @Test
    @DisplayName("Should fail to create customer with invalid data")
    void shouldFailToCreateCustomerWithInvalidData() throws Exception {
        // Given
        CustomerDto.CreateRequest request = new CustomerDto.CreateRequest();
        request.setEmail("invalid-email"); // Invalid email

        // When & Then
        mockMvc.perform(post("/api/customers")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(customerService);
    }

    @Test
    @DisplayName("Should update customer successfully")
    void shouldUpdateCustomerSuccessfully() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        CustomerDto.UpdateRequest request = new CustomerDto.UpdateRequest();
        request.setName("Updated Name");
        request.setEmail("updated@example.com");

        CustomerDto.Response response = new CustomerDto.Response();
        response.setId(id.toString());
        response.setName("Updated Name");

        when(customerService.updateCustomer(eq(id), any(CustomerDto.UpdateRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/customers/{id}", id)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));

        verify(customerService, times(1)).updateCustomer(eq(id), any());
    }

    @Test
    @DisplayName("Should verify KYC successfully")
    void shouldVerifyKycSuccessfully() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        doNothing().when(customerService).verifyKyc(id);

        // When & Then
        mockMvc.perform(post("/api/customers/{id}/kyc", id).with(csrf()))
                .andExpect(status().isNoContent());

        verify(customerService, times(1)).verifyKyc(id);
    }

    @Test
    @DisplayName("Should delete customer successfully")
    void shouldDeleteCustomerSuccessfully() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        doNothing().when(customerService).deleteCustomerById(id);

        // When & Then
        mockMvc.perform(delete("/api/customers/{id}", id).with(csrf()))
                .andExpect(status().isNoContent());

        verify(customerService, times(1)).deleteCustomerById(id);
    }

    @Test
    @DisplayName("Should handle CustomerNotFoundException")
    void shouldHandleCustomerNotFoundException() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        when(customerService.getCustomerById(id)).thenThrow(new CustomerNotFoundException("Customer not found"));

        // When & Then
        mockMvc.perform(get("/api/customers/{id}", id).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Customer not found"));
    }

    @Test
    @DisplayName("Should handle CustomerAlreadyExistsException")
    void shouldHandleCustomerAlreadyExistsException() throws Exception {
        // Given
        CustomerDto.CreateRequest request = new CustomerDto.CreateRequest();
        request.setTenantId("t1");
        request.setName("Existent");
        request.setEmail("ext@example.com");

        when(customerService.createCustomer(any())).thenThrow(new CustomerAlreadyExistsException("Already exists"));

        // When & Then
        mockMvc.perform(post("/api/customers")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_ALREADY_EXISTS"));
    }

    @Test
    @DisplayName("Should handle KycAlreadyVerifiedException")
    void shouldHandleKycAlreadyVerifiedException() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        doThrow(new KycAlreadyVerifiedException("Already verified")).when(customerService).verifyKyc(id);

        // When & Then
        mockMvc.perform(post("/api/customers/{id}/kyc", id).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("KYC_ALREADY_VERIFIED"));
    }

    @Test
    @DisplayName("Should handle DomainException")
    void shouldHandleDomainException() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        when(customerService.getCustomerById(id)).thenThrow(new DomainException("Domain error"));

        // When & Then
        mockMvc.perform(get("/api/customers/{id}", id).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Domain Error"));
    }

    @Test
    @DisplayName("Should handle DataIntegrityViolationException")
    void shouldHandleDataIntegrityViolationException() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        when(customerService.getCustomerById(id)).thenThrow(new DataIntegrityViolationException("Conflict"));

        // When & Then
        mockMvc.perform(get("/api/customers/{id}", id).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DATA_INTEGRITY_VIOLATION"));
    }

    @Test
    @DisplayName("Should handle RuntimeException")
    void shouldHandleRuntimeException() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        when(customerService.getCustomerById(id)).thenThrow(new RuntimeException("Unexpected"));

        // When & Then
        mockMvc.perform(get("/api/customers/{id}", id).with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("RUNTIME_ERROR"));
    }

    @Test
    @DisplayName("Should handle generic Exception")
    void shouldHandleCheckedException() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        when(customerService.getCustomerById(id)).thenThrow(new RuntimeException("Checked exception wrap"));

        // When & Then
        mockMvc.perform(get("/api/customers/{id}", id).with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("RUNTIME_ERROR"));
    }
}
