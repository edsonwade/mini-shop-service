package code.with.vanilson.market.orders.api;

import code.with.vanilson.market.identity.domain.UserRepository;
import code.with.vanilson.market.identity.infrastructure.CustomUserDetailsService;
import code.with.vanilson.market.identity.infrastructure.JwtAuthenticationFilter;
import code.with.vanilson.market.identity.infrastructure.JwtProvider;
import code.with.vanilson.market.orders.application.OrderService;
import code.with.vanilson.market.shared.infrastructure.IdempotencyFilter;
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

@WebMvcTest(controllers = OrderController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@WithMockUser
@DisplayName("Order Controller Tests")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

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
    @DisplayName("Should place order successfully")
    void shouldPlaceOrderSuccessfully() throws Exception {
        // Given
        OrderDto.CreateRequest request = new OrderDto.CreateRequest();
        request.setTenantId("tenant-1");
        request.setCustomerId(UUID.randomUUID());
        OrderDto.OrderItemRequest item = new OrderDto.OrderItemRequest();
        item.setProductId(UUID.randomUUID());
        item.setQuantity(2);
        request.setItems(List.of(item));

        OrderDto.Response response = new OrderDto.Response();
        response.setId(UUID.randomUUID());
        response.setStatus("PLACED");
        response.setTotalAmount(new BigDecimal("100.00"));

        when(orderService.placeOrder(any(OrderDto.CreateRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andExpect(jsonPath("$.totalAmount").value(100.00));

        verify(orderService, times(1)).placeOrder(any());
    }

    @Test
    @DisplayName("Should return 400 when order request is missing items")
    void shouldReturn400WhenOrderRequestIsInvalid() throws Exception {
        // Given
        OrderDto.CreateRequest request = new OrderDto.CreateRequest();
        request.setTenantId("t1");
        request.setCustomerId(UUID.randomUUID());
        request.setItems(List.of()); // Empty items

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName("Should get order by ID successfully")
    void shouldGetOrderSuccessfully() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        OrderDto.Response response = new OrderDto.Response();
        response.setId(id);
        response.setStatus("COMPLETED");

        when(orderService.getOrder(id)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/orders/{id}", id).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(orderService, times(1)).getOrder(id);
    }

    @Test
    @DisplayName("Should cancel order successfully")
    void shouldCancelOrderSuccessfully() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        doNothing().when(orderService).cancelOrder(id);

        // When & Then
        mockMvc.perform(post("/api/orders/{id}/cancel", id).with(csrf()))
                .andExpect(status().isNoContent());

        verify(orderService, times(1)).cancelOrder(id);
    }

    @Test
    @DisplayName("Should handle KafkaException")
    void shouldHandleKafkaException() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        when(orderService.getOrder(id)).thenThrow(new org.springframework.kafka.KafkaException("Kafka down"));

        // When & Then
        mockMvc.perform(get("/api/orders/{id}", id).with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("KAFKA_ERROR"));
    }

    @Test
    @DisplayName("Should return 400 when order item quantity is invalid")
    void shouldReturn400WhenQuantityIsInvalid() throws Exception {
        // Given
        OrderDto.CreateRequest request = new OrderDto.CreateRequest();
        request.setTenantId("t1");
        request.setCustomerId(UUID.randomUUID());
        OrderDto.OrderItemRequest item = new OrderDto.OrderItemRequest();
        item.setProductId(UUID.randomUUID());
        item.setQuantity(0); // Invalid quantity < 1
        request.setItems(List.of(item));

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
