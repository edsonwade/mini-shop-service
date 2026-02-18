package code.with.vanilson.market.payments.api;

import code.with.vanilson.market.identity.domain.UserRepository;
import code.with.vanilson.market.identity.infrastructure.CustomUserDetailsService;
import code.with.vanilson.market.identity.infrastructure.JwtAuthenticationFilter;
import code.with.vanilson.market.identity.infrastructure.JwtProvider;
import code.with.vanilson.market.payments.application.PaymentService;
import code.with.vanilson.market.shared.infrastructure.IdempotencyFilter;
import code.with.vanilson.market.shared.infrastructure.exception.CurrencyMismatchException;
import code.with.vanilson.market.shared.infrastructure.exception.GlobalExceptionHandler;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PaymentController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@WithMockUser
@DisplayName("Payment Controller Tests")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

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
    @DisplayName("Should list all payments successfully")
    void shouldListAllPaymentsSuccessfully() throws Exception {
        // Given
        PaymentDto.Response response = new PaymentDto.Response();
        response.setId(UUID.randomUUID());
        response.setAmount(new BigDecimal("50.00"));
        response.setStatus("COMPLETED");

        when(paymentService.listAllPayments()).thenReturn(List.of(response));

        // When & Then
        mockMvc.perform(get("/api/payments").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));

        verify(paymentService, times(1)).listAllPayments();
    }

    @Test
    @DisplayName("Should get payment by order ID successfully")
    void shouldGetPaymentByOrderIdSuccessfully() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();
        PaymentDto.Response response = new PaymentDto.Response();
        response.setOrderId(orderId);
        response.setStatus("PENDING");

        when(paymentService.getPaymentByOrderId(orderId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/payments/order/{orderId}", orderId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()));

        verify(paymentService, times(1)).getPaymentByOrderId(orderId);
    }

    @Test
    @DisplayName("Should process payment successfully")
    void shouldProcessPaymentSuccessfully() throws Exception {
        // Given
        PaymentDto.ProcessRequest request = new PaymentDto.ProcessRequest();
        request.setOrderId(UUID.randomUUID());
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setPaymentMethod("CREDIT_CARD");

        PaymentDto.Response response = new PaymentDto.Response();
        response.setId(UUID.randomUUID());
        response.setStatus("SUCCESS");

        when(paymentService.processPayment(any(PaymentDto.ProcessRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(paymentService, times(1)).processPayment(any());
    }

    @Test
    @DisplayName("Should refund order successfully")
    void shouldRefundOrderSuccessfully() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();
        doNothing().when(paymentService).refundOrder(orderId);

        // When & Then
        mockMvc.perform(post("/api/payments/refund/{orderId}", orderId).with(csrf()))
                .andExpect(status().isNoContent());

        verify(paymentService, times(1)).refundOrder(orderId);
    }

    @Test
    @DisplayName("Should get payment by payment ID successfully")
    void shouldGetPaymentByIdSuccessfully() throws Exception {
        // Given
        UUID paymentId = UUID.randomUUID();
        PaymentDto.Response response = new PaymentDto.Response();
        response.setId(paymentId);

        when(paymentService.getPaymentById(paymentId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/payments/{paymentId}", paymentId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentId.toString()));

        verify(paymentService, times(1)).getPaymentById(paymentId);
    }

    @Test
    @DisplayName("Should handle CurrencyMismatchException")
    void shouldHandleCurrencyMismatchException() throws Exception {
        // Given
        PaymentDto.ProcessRequest request = new PaymentDto.ProcessRequest();
        request.setOrderId(UUID.randomUUID());
        request.setAmount(new BigDecimal("10.00"));
        request.setCurrency("EUR");

        when(paymentService.processPayment(any())).thenThrow(new CurrencyMismatchException("Currency mismatch"));

        // When & Then
        mockMvc.perform(post("/api/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("CURRENCY_MISMATCH"));
    }

    @Test
    @DisplayName("Should return 400 when payment processing request is invalid")
    void shouldReturn400WhenRequestIsInvalid() throws Exception {
        // Given
        PaymentDto.ProcessRequest request = new PaymentDto.ProcessRequest();
        request.setAmount(new BigDecimal("0.00")); // Invalid amount

        // When & Then
        mockMvc.perform(post("/api/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(paymentService);
    }
}
