package code.with.vanilson.market.products.application;

import code.with.vanilson.market.products.api.ProductDto;
import code.with.vanilson.market.products.domain.Product;
import code.with.vanilson.market.products.domain.ProductRepository;
import code.with.vanilson.market.shared.domain.DomainException;
import code.with.vanilson.market.shared.domain.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final String TENANT_ID = UUID.randomUUID().toString();
    private static final String SKU = "PROD-001";
    private static final String NAME = "Test Product";
    private static final String DESCRIPTION = "Test product description";
    private static final BigDecimal PRICE = new BigDecimal("99.99");
    private static final String CURRENCY = "USD";
    private static final int INVENTORY_COUNT = 100;

    // ========================= Create Product Tests =========================

    @Test
    @DisplayName("should successfully create a new product with valid request")
    void testCreateProduct_Success() {
        // Arrange
        ProductDto.CreateRequest request = new ProductDto.CreateRequest();
        request.setTenantId(TENANT_ID);
        request.setName(NAME);
        request.setDescription(DESCRIPTION);
        request.setSku(SKU);
        request.setPrice(PRICE);
        request.setCurrency(CURRENCY);
        request.setInventoryCount(INVENTORY_COUNT);

        Product savedProduct = Product.builder()

                .tenantId(TENANT_ID)
                .name(NAME)
                .description(DESCRIPTION)
                .sku(SKU)
                .price(new Money(PRICE, CURRENCY))
                .inventoryCount(INVENTORY_COUNT)
                .build();
        ReflectionTestUtils.setField(savedProduct, "id", PRODUCT_ID);

        when(productRepository.findBySku(SKU)).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // Act
        ProductDto.Response response = productService.createProduct(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(PRODUCT_ID.toString());
        assertThat(response.getName()).isEqualTo(NAME);
        assertThat(response.getSku()).isEqualTo(SKU);
        assertThat(response.getPrice()).isEqualTo(PRICE);
        assertThat(response.getCurrency()).isEqualTo(CURRENCY);
        assertThat(response.getInventoryCount()).isEqualTo(INVENTORY_COUNT);

        verify(productRepository, times(1)).findBySku(SKU);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("should throw DomainException when SKU already exists")
    void testCreateProduct_SkuAlreadyExists() {
        // Arrange
        ProductDto.CreateRequest request = new ProductDto.CreateRequest();
        request.setTenantId(TENANT_ID);
        request.setName(NAME);
        request.setDescription(DESCRIPTION);
        request.setSku(SKU);
        request.setPrice(PRICE);
        request.setCurrency(CURRENCY);
        request.setInventoryCount(INVENTORY_COUNT);

        Product existingProduct = Product.builder()
                .sku(SKU)
                .build();
        ReflectionTestUtils.setField(existingProduct, "id", PRODUCT_ID);

        when(productRepository.findBySku(SKU)).thenReturn(Optional.of(existingProduct));

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(DomainException.class)
                .hasMessage("Product with this SKU already exists");

        verify(productRepository, times(1)).findBySku(SKU);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("should successfully create product with minimal data")
    void testCreateProduct_MinimalData() {
        // Arrange
        ProductDto.CreateRequest request = new ProductDto.CreateRequest();
        request.setTenantId(TENANT_ID);
        request.setName("Minimal Product");
        request.setDescription("");
        request.setSku("MIN-SKU");
        request.setPrice(new BigDecimal("10.00"));
        request.setCurrency("USD");
        request.setInventoryCount(0);

        Product savedProduct = Product.builder()

                .tenantId(TENANT_ID)
                .name("Minimal Product")
                .description("")
                .sku("MIN-SKU")
                .price(new Money(new BigDecimal("10.00"), "USD"))
                .inventoryCount(0)
                .build();
        ReflectionTestUtils.setField(savedProduct, "id", PRODUCT_ID);

        when(productRepository.findBySku("MIN-SKU")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // Act
        ProductDto.Response response = productService.createProduct(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getInventoryCount()).isEqualTo(0);
        assertThat(response.getPrice()).isEqualTo(new BigDecimal("10.00"));

        verify(productRepository, times(1)).findBySku("MIN-SKU");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    // ========================= Get Product Tests =========================

    @Test
    @DisplayName("should successfully retrieve product by ID")
    void testGetProduct_Success() {
        // Arrange
        Product product = Product.builder()

                .tenantId(TENANT_ID)
                .name(NAME)
                .description(DESCRIPTION)
                .sku(SKU)
                .price(new Money(PRICE, CURRENCY))
                .inventoryCount(INVENTORY_COUNT)
                .build();
        ReflectionTestUtils.setField(product, "id", PRODUCT_ID);

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        // Act
        ProductDto.Response response = productService.getProduct(PRODUCT_ID);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(PRODUCT_ID.toString());
        assertThat(response.getName()).isEqualTo(NAME);
        assertThat(response.getSku()).isEqualTo(SKU);
        assertThat(response.getPrice()).isEqualTo(PRICE);

        verify(productRepository, times(1)).findById(PRODUCT_ID);
    }

    @Test
    @DisplayName("should throw DomainException when product not found")
    void testGetProduct_NotFound() {
        // Arrange
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.getProduct(PRODUCT_ID))
                .isInstanceOf(DomainException.class)
                .hasMessage("Product not found");

        verify(productRepository, times(1)).findById(PRODUCT_ID);
    }

    @Test
    @DisplayName("should correctly map product to response DTO")
    void testGetProduct_MappingCorrectness() {
        // Arrange
        Product product = Product.builder()
                .tenantId(TENANT_ID)
                .name("Premium Product")
                .description("High quality item")
                .sku("PREMIUM-001")
                .price(new Money(new BigDecimal("199.99"), "EUR"))
                .inventoryCount(50)
                .build();
        ReflectionTestUtils.setField(product, "id", PRODUCT_ID);

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        // Act
        ProductDto.Response response = productService.getProduct(PRODUCT_ID);

        // Assert
        assertThat(response.getName()).isEqualTo("Premium Product");
        assertThat(response.getSku()).isEqualTo("PREMIUM-001");
        assertThat(response.getCurrency()).isEqualTo("EUR");
        assertThat(response.getInventoryCount()).isEqualTo(50);

        verify(productRepository, times(1)).findById(PRODUCT_ID);
    }

    // ========================= Get All Products Tests =========================

    @Test
    @DisplayName("should successfully retrieve all products")
    void testGetAllProducts_Success() {
        // Arrange
        Product product1 = Product.builder()
                
                .name("Product 1")
                .sku("SKU-001")
                .price(new Money(new BigDecimal("50.00"), "USD"))
                .inventoryCount(100)
                .build();
        ReflectionTestUtils.setField(product1, "id", PRODUCT_ID);

        Product product2 = Product.builder()
                
                .name("Product 2")
                .sku("SKU-002")
                .price(new Money(new BigDecimal("75.00"), "USD"))
                .inventoryCount(200)
                .build();
        ReflectionTestUtils.setField(product2, "id", PRODUCT_ID);

        Product product3 = Product.builder()
                
                .name("Product 3")
                .sku("SKU-003")
                .price(new Money(new BigDecimal("25.00"), "USD"))
                .inventoryCount(50)
                .build();
        ReflectionTestUtils.setField(product3, "id", PRODUCT_ID);

        List<Product> products = Arrays.asList(product1, product2, product3);

        when(productRepository.findAll()).thenReturn(products);

        // Act
        List<ProductDto.Response> responses = productService.getAllProducts();

        // Assert
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(3);
        assertThat(responses).extracting("name")
                .containsExactly("Product 1", "Product 2", "Product 3");
        assertThat(responses).extracting("inventoryCount")
                .containsExactly(100, 200, 50);

        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("should return empty list when no products exist")
    void testGetAllProducts_EmptyList() {
        // Arrange
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<ProductDto.Response> responses = productService.getAllProducts();

        // Assert
        assertThat(responses).isNotNull();
        assertThat(responses).isEmpty();

        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("should successfully retrieve all products with single product")
    void testGetAllProducts_SingleProduct() {
        // Arrange
        Product product = Product.builder()
                .name(NAME)
                .sku(SKU)
                .price(new Money(PRICE, CURRENCY))
                .inventoryCount(INVENTORY_COUNT)
                .build();
        ReflectionTestUtils.setField(product, "id", PRODUCT_ID);

        when(productRepository.findAll()).thenReturn(Collections.singletonList(product));

        // Act
        List<ProductDto.Response> responses = productService.getAllProducts();

        // Assert
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo(NAME);
        assertThat(responses.get(0).getSku()).isEqualTo(SKU);

        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("should correctly map multiple products in getAllProducts")
    void testGetAllProducts_CorrectMapping() {
        // Arrange
        Product product1 = Product.builder()
                
                .name("Product A")
                .description("Description A")
                .sku("SKU-A")
                .price(new Money(new BigDecimal("100.00"), "USD"))
                .inventoryCount(10)
                .build();
        ReflectionTestUtils.setField(product1, "id", PRODUCT_ID);

        Product product2 = Product.builder()
                
                .name("Product B")
                .description("Description B")
                .sku("SKU-B")
                .price(new Money(new BigDecimal("200.00"), "EUR"))
                .inventoryCount(20)
                .build();
        ReflectionTestUtils.setField(product2, "id", PRODUCT_ID);

        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));

        // Act
        List<ProductDto.Response> responses = productService.getAllProducts();

        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getPrice()).isEqualTo(new BigDecimal("100.00"));
        assertThat(responses.get(0).getCurrency()).isEqualTo("USD");
        assertThat(responses.get(1).getPrice()).isEqualTo(new BigDecimal("200.00"));
        assertThat(responses.get(1).getCurrency()).isEqualTo("EUR");

        verify(productRepository, times(1)).findAll();
    }

    // ========================= Edge Cases and Exception Handling Tests =========================

    @Test
    @DisplayName("should handle product with zero price")
    void testCreateProduct_ZeroPrice() {
        // Arrange
        ProductDto.CreateRequest request = new ProductDto.CreateRequest();
        request.setTenantId(TENANT_ID);
        request.setName("Free Product");
        request.setDescription("Free item");
        request.setSku("FREE-001");
        request.setPrice(BigDecimal.ZERO);
        request.setCurrency("USD");
        request.setInventoryCount(100);

        Product savedProduct = Product.builder()

                .tenantId(TENANT_ID)
                .name("Free Product")
                .price(new Money(BigDecimal.ZERO, "USD"))
                .inventoryCount(100)
                .build();
        ReflectionTestUtils.setField(savedProduct, "id", PRODUCT_ID);

        when(productRepository.findBySku("FREE-001")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // Act
        ProductDto.Response response = productService.createProduct(request);

        // Assert
        assertThat(response.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(productRepository, times(1)).findBySku("FREE-001");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("should handle product with large inventory count")
    void testCreateProduct_LargeInventory() {
        // Arrange
        ProductDto.CreateRequest request = new ProductDto.CreateRequest();
        request.setTenantId(TENANT_ID);
        request.setName("Popular Product");
        request.setDescription("In high demand");
        request.setSku("POPULAR-001");
        request.setPrice(new BigDecimal("49.99"));
        request.setCurrency("USD");
        request.setInventoryCount(1000000);

        Product savedProduct = Product.builder()
                .tenantId(TENANT_ID)
                .name("Popular Product")
                .sku("POPULAR-001")
                .price(new Money(new BigDecimal("49.99"), "USD"))
                .inventoryCount(1000000)
                .build();
        ReflectionTestUtils.setField(savedProduct, "id", PRODUCT_ID);

        when(productRepository.findBySku("POPULAR-001")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // Act
        ProductDto.Response response = productService.createProduct(request);

        // Assert
        assertThat(response.getInventoryCount()).isEqualTo(1000000);

        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("should handle multiple currency types")
    void testGetProduct_MultipleCurrencies() {
        // Arrange
        Product eurProduct = Product.builder()
                .name("EUR Product")
                .sku("EUR-001")
                .price(new Money(new BigDecimal("50.00"), "EUR"))
                .build();
        ReflectionTestUtils.setField(eurProduct, "id", PRODUCT_ID);

        Product gbpProduct = Product.builder()
                .name("GBP Product")
                .sku("GBP-001")
                .price(new Money(new BigDecimal("50.00"), "GBP"))
                .build();
        ReflectionTestUtils.setField(gbpProduct, "id", PRODUCT_ID);

        when(productRepository.findAll()).thenReturn(Arrays.asList(eurProduct, gbpProduct));

        // Act
        List<ProductDto.Response> responses = productService.getAllProducts();

        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses).extracting("currency")
                .containsExactly("EUR", "GBP");

        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("should verify repository interactions during creation")
    void testCreateProduct_VerifyRepositoryInteractions() {
        // Arrange
        ProductDto.CreateRequest request = new ProductDto.CreateRequest();
        request.setTenantId(TENANT_ID);
        request.setName(NAME);
        request.setDescription(DESCRIPTION);
        request.setSku(SKU);
        request.setPrice(PRICE);
        request.setCurrency(CURRENCY);
        request.setInventoryCount(INVENTORY_COUNT);

        Product savedProduct = Product.builder()
                .tenantId(TENANT_ID)
                .name(NAME)
                .description(DESCRIPTION)
                .sku(SKU)
                .price(new Money(PRICE, CURRENCY))
                .inventoryCount(INVENTORY_COUNT)
                .build();
        ReflectionTestUtils.setField(savedProduct, "id", PRODUCT_ID);

        when(productRepository.findBySku(SKU)).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // Act
        productService.createProduct(request);

        // Assert
        verify(productRepository).findBySku(SKU);
        verify(productRepository).save(any(Product.class));
        verifyNoMoreInteractions(productRepository);
    }
}

