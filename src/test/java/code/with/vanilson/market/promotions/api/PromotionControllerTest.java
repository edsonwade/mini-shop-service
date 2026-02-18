package code.with.vanilson.market.promotions.api;

import code.with.vanilson.market.identity.domain.UserRepository;
import code.with.vanilson.market.identity.infrastructure.CustomUserDetailsService;
import code.with.vanilson.market.identity.infrastructure.JwtAuthenticationFilter;
import code.with.vanilson.market.identity.infrastructure.JwtProvider;
import code.with.vanilson.market.promotions.application.PromotionService;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PromotionController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@WithMockUser
@DisplayName("Promotion Controller Tests")
class PromotionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PromotionService promotionService;

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
    @DisplayName("Should create coupon successfully")
    void shouldCreateCouponSuccessfully() throws Exception {
        // Given
        PromotionDto.CreateCouponRequest request = new PromotionDto.CreateCouponRequest();
        request.setCode("SAVE20");
        request.setDiscountAmount(new BigDecimal("20.00"));
        request.setCurrency("USD");
        request.setExpiryDate(Instant.now().plus(10, ChronoUnit.DAYS));

        PromotionDto.Response response = new PromotionDto.Response();
        response.setCode("SAVE20");
        response.setActive(true);

        when(promotionService.createCoupon(any(PromotionDto.CreateCouponRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/promotions/coupons")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SAVE20"))
                .andExpect(jsonPath("$.active").value(true));

        verify(promotionService, times(1)).createCoupon(any());
    }

    @Test
    @DisplayName("Should return 400 when coupon creation request is invalid")
    void shouldReturn400WhenCreateCouponIsInvalid() throws Exception {
        // Given
        PromotionDto.CreateCouponRequest request = new PromotionDto.CreateCouponRequest();
        request.setCode(""); // Invalid code

        // When & Then
        mockMvc.perform(post("/api/promotions/coupons")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(promotionService);
    }

    @Test
    @DisplayName("Should return 400 when coupon expiry date is in the past")
    void shouldReturn400WhenExpiryDateIsInPast() throws Exception {
        // Given
        PromotionDto.CreateCouponRequest request = new PromotionDto.CreateCouponRequest();
        request.setCode("EXPIRED");
        request.setDiscountAmount(new BigDecimal("10.00"));
        request.setCurrency("USD");
        request.setExpiryDate(Instant.now().minus(1, ChronoUnit.DAYS)); // Past date

        // When & Then
        mockMvc.perform(post("/api/promotions/coupons")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(promotionService);
    }
}
