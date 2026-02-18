package code.with.vanilson.market.products.api;

import code.with.vanilson.market.identity.domain.UserRepository;
import code.with.vanilson.market.identity.infrastructure.CustomUserDetailsService;
import code.with.vanilson.market.identity.infrastructure.JwtAuthenticationFilter;
import code.with.vanilson.market.identity.infrastructure.JwtProvider;
import code.with.vanilson.market.products.application.ProductService;
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

@WebMvcTest(controllers = ProductController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@WithMockUser
@DisplayName("Product Controller Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

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
    @DisplayName("Should create product successfully")
    void shouldCreateProductSuccessfully() throws Exception {
        // Given
        ProductDto.CreateRequest request = new ProductDto.CreateRequest();
        request.setTenantId("t1");
        request.setName("Apple");
        request.setSku("SKU-APP");
        request.setPrice(new BigDecimal("1.50"));
        request.setCurrency("USD");
        request.setInventoryCount(100);

        ProductDto.Response response = new ProductDto.Response();
        response.setId(UUID.randomUUID().toString());
        response.setName("Apple");

        when(productService.createProduct(any(ProductDto.CreateRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Apple"));

        verify(productService, times(1)).createProduct(any());
    }

    @Test
    @DisplayName("Should return 400 when product creation request is invalid")
    void shouldReturn400WhenCreateRequestIsInvalid() throws Exception {
        // Given
        ProductDto.CreateRequest request = new ProductDto.CreateRequest();
        request.setName(""); // Invalid name

        // When & Then
        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName("Should get product by ID successfully")
    void shouldGetProductByIdSuccessfully() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        ProductDto.Response response = new ProductDto.Response();
        response.setId(id.toString());
        response.setName("Orange");

        when(productService.getProduct(id)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/products/{id}", id).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(productService, times(1)).getProduct(id);
    }

    @Test
    @DisplayName("Should list all products successfully")
    void shouldListAllProductsSuccessfully() throws Exception {
        // Given
        ProductDto.Response response = new ProductDto.Response();
        response.setName("Banana");

        when(productService.getAllProducts()).thenReturn(List.of(response));

        // When & Then
        mockMvc.perform(get("/api/products").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Banana"));

        verify(productService, times(1)).getAllProducts();
    }
}
