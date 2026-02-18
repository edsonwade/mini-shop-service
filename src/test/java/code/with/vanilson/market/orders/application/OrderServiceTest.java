package code.with.vanilson.market.orders.application;

import code.with.vanilson.market.customers.domain.CustomerRepository;
import code.with.vanilson.market.events.domain.EventProducer;
import code.with.vanilson.market.orders.api.OrderDto;
import code.with.vanilson.market.orders.domain.Order;
import code.with.vanilson.market.orders.domain.OrderItem;
import code.with.vanilson.market.orders.domain.OrderRepository;
import code.with.vanilson.market.orders.domain.OrderStatus;
import code.with.vanilson.market.products.domain.Product;
import code.with.vanilson.market.products.domain.ProductRepository;
import code.with.vanilson.market.promotions.domain.Coupon;
import code.with.vanilson.market.promotions.domain.CouponRepository;
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
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private EventProducer eventProducer;

    @InjectMocks
    private OrderService orderService;

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final String TENANT_ID = UUID.randomUUID().toString();
    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final String COUPON_CODE = "SAVE20";

    // ========================= Place Order Tests =========================

    @Test
    @DisplayName("should successfully place order with single product")
    void testPlaceOrder_Success() {
        // Arrange
        OrderDto.OrderItemRequest itemRequest = new OrderDto.OrderItemRequest();
        itemRequest.setProductId(PRODUCT_ID);
        itemRequest.setQuantity(2);

        OrderDto.CreateRequest request = new OrderDto.CreateRequest();
        request.setTenantId(TENANT_ID);
        request.setCustomerId(CUSTOMER_ID);
        request.setItems(Collections.singletonList(itemRequest));
        request.setCouponCode(null);

        Product product = Product.builder()
                .sku("PROD-001")
                .price(new Money(new BigDecimal("50.00"), "USD"))
                .inventoryCount(100)
                .build();
        ReflectionTestUtils.setField(product, "id", PRODUCT_ID);

        // Create OrderItem for the saved order
        OrderItem orderItem = OrderItem.builder()
                .productId(PRODUCT_ID)
                .sku("PROD-001")
                .quantity(2)
                .unitPrice(new Money(new BigDecimal("50.00"), "USD"))
                .build();
        ReflectionTestUtils.setField(orderItem, "id", UUID.randomUUID());

        // Total: 50.00 * 2 = 100.00
        Order savedOrder = Order.builder()
                .tenantId(TENANT_ID)
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.PLACED)
                .createdAt(Instant.now())
                .items(Collections.singletonList(orderItem))
                .totalAmount(new Money(new BigDecimal("100.00"), "USD"))
                .build();
        ReflectionTestUtils.setField(savedOrder, "id", ORDER_ID);

        when(customerRepository.existsById(CUSTOMER_ID)).thenReturn(true);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        OrderDto.Response response = orderService.placeOrder(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(ORDER_ID);
        assertThat(response.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(response.getStatus()).isEqualTo("PLACED");
        assertThat(response.getTotalAmount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getQuantity()).isEqualTo(2);

        verify(customerRepository, times(1)).existsById(CUSTOMER_ID);
        verify(productRepository, times(1)).findById(PRODUCT_ID);
        verify(productRepository, times(1)).save(any(Product.class));
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(eventProducer, times(1)).publish(anyString(), any());
    }

    @Test
    @DisplayName("should throw DomainException when customer not found during order placement")
    void testPlaceOrder_CustomerNotFound() {
        // Arrange
        OrderDto.OrderItemRequest itemRequest = new OrderDto.OrderItemRequest();
        itemRequest.setProductId(PRODUCT_ID);
        itemRequest.setQuantity(1);

        OrderDto.CreateRequest request = new OrderDto.CreateRequest();
        request.setTenantId(TENANT_ID);
        request.setCustomerId(CUSTOMER_ID);
        request.setItems(Collections.singletonList(itemRequest));

        when(customerRepository.existsById(CUSTOMER_ID)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(DomainException.class)
                .hasMessage("Customer not found");

        verify(customerRepository, times(1)).existsById(CUSTOMER_ID);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("should throw DomainException when product not found during order placement")
    void testPlaceOrder_ProductNotFound() {
        // Arrange
        OrderDto.OrderItemRequest itemRequest = new OrderDto.OrderItemRequest();
        itemRequest.setProductId(PRODUCT_ID);
        itemRequest.setQuantity(1);

        OrderDto.CreateRequest request = new OrderDto.CreateRequest();
        request.setTenantId(TENANT_ID);
        request.setCustomerId(CUSTOMER_ID);
        request.setItems(Collections.singletonList(itemRequest));

        when(customerRepository.existsById(CUSTOMER_ID)).thenReturn(true);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(DomainException.class)
                .hasMessage("Product not found: " + PRODUCT_ID);

        verify(customerRepository, times(1)).existsById(CUSTOMER_ID);
        verify(productRepository, times(1)).findById(PRODUCT_ID);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("should successfully place order with multiple products")
    void testPlaceOrder_MultipleProducts() {
        // Arrange
        UUID productId2 = UUID.randomUUID();

        OrderDto.OrderItemRequest item1 = new OrderDto.OrderItemRequest();
        item1.setProductId(PRODUCT_ID);
        item1.setQuantity(2);

        OrderDto.OrderItemRequest item2 = new OrderDto.OrderItemRequest();
        item2.setProductId(productId2);
        item2.setQuantity(3);

        OrderDto.CreateRequest request = new OrderDto.CreateRequest();
        request.setTenantId(TENANT_ID);
        request.setCustomerId(CUSTOMER_ID);
        request.setItems(Arrays.asList(item1, item2));

        Product product1 = Product.builder()
                .sku("PROD-001")
                .price(new Money(new BigDecimal("50.00"), "USD"))
                .inventoryCount(100)
                .build();
        ReflectionTestUtils.setField(product1, "id", PRODUCT_ID);

        Product product2 = Product.builder()
                .sku("PROD-002")
                .price(new Money(new BigDecimal("75.00"), "USD"))
                .inventoryCount(100)
                .build();
        ReflectionTestUtils.setField(product2, "id", productId2);

        // Create OrderItems for the saved order
        OrderItem orderItem1 = OrderItem.builder()
                .productId(PRODUCT_ID)
                .sku("PROD-001")
                .quantity(2)
                .unitPrice(new Money(new BigDecimal("50.00"), "USD"))
                .build();
        ReflectionTestUtils.setField(orderItem1, "id", UUID.randomUUID());

        OrderItem orderItem2 = OrderItem.builder()
                .productId(productId2)
                .sku("PROD-002")
                .quantity(3)
                .unitPrice(new Money(new BigDecimal("75.00"), "USD"))
                .build();
        ReflectionTestUtils.setField(orderItem2, "id", UUID.randomUUID());

        // Total: (50.00 * 2) + (75.00 * 3) = 100 + 225 = 325
        Order savedOrder = Order.builder()
                .tenantId(TENANT_ID)
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.PLACED)
                .createdAt(Instant.now())
                .items(Arrays.asList(orderItem1, orderItem2))
                .totalAmount(new Money(new BigDecimal("325.00"), "USD"))
                .build();
        ReflectionTestUtils.setField(savedOrder, "id", ORDER_ID);

        when(customerRepository.existsById(CUSTOMER_ID)).thenReturn(true);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product1));
        when(productRepository.findById(productId2)).thenReturn(Optional.of(product2));
        when(productRepository.save(any(Product.class))).thenReturn(product1).thenReturn(product2);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        OrderDto.Response response = orderService.placeOrder(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getTotalAmount()).isEqualTo(new BigDecimal("325.00"));
        assertThat(response.getItems()).extracting("quantity")
                .containsExactly(2, 3);

        verify(productRepository, times(2)).findById(any(UUID.class));
        verify(productRepository, times(2)).save(any(Product.class));
    }

    @Test
    @DisplayName("should successfully apply coupon when valid coupon code provided")
    void testPlaceOrder_WithValidCoupon() {
        // Arrange
        OrderDto.OrderItemRequest itemRequest = new OrderDto.OrderItemRequest();
        itemRequest.setProductId(PRODUCT_ID);
        itemRequest.setQuantity(1);

        OrderDto.CreateRequest request = new OrderDto.CreateRequest();
        request.setTenantId(TENANT_ID);
        request.setCustomerId(CUSTOMER_ID);
        request.setItems(Collections.singletonList(itemRequest));
        request.setCouponCode(COUPON_CODE);

        Product product = Product.builder()
                .sku("PROD-001")
                .price(new Money(new BigDecimal("100.00"), "USD"))
                .inventoryCount(100)
                .build();
        ReflectionTestUtils.setField(product, "id", PRODUCT_ID);

        Coupon coupon = Coupon.builder()
                .code(COUPON_CODE)
                .discount(new Money(new BigDecimal("20.00"), "USD"))
                .expiryDate(Instant.now().plusSeconds(86400))
                .active(true)
                .build();
        ReflectionTestUtils.setField(coupon, "id", UUID.randomUUID());

        // Create OrderItem for the saved order
        OrderItem orderItem = OrderItem.builder()
                .productId(PRODUCT_ID)
                .sku("PROD-001")
                .quantity(1)
                .unitPrice(new Money(new BigDecimal("100.00"), "USD"))
                .build();
        ReflectionTestUtils.setField(orderItem, "id", UUID.randomUUID());

        // Total: 100.00 - 20.00 = 80.00
        Order savedOrder = Order.builder()
                .tenantId(TENANT_ID)
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.PLACED)
                .createdAt(Instant.now())
                .items(Collections.singletonList(orderItem))
                .totalAmount(new Money(new BigDecimal("80.00"), "USD"))
                .build();
        ReflectionTestUtils.setField(savedOrder, "id", ORDER_ID);

        when(customerRepository.existsById(CUSTOMER_ID)).thenReturn(true);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(couponRepository.findByCode(COUPON_CODE)).thenReturn(Optional.of(coupon));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        OrderDto.Response response = orderService.placeOrder(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        verify(couponRepository, times(1)).findByCode(COUPON_CODE);
        verify(eventProducer, times(1)).publish(anyString(), any());
    }

    @Test
    @DisplayName("should throw DomainException when invalid coupon code provided")
    void testPlaceOrder_InvalidCouponCode() {
        // Arrange
        OrderDto.OrderItemRequest itemRequest = new OrderDto.OrderItemRequest();
        itemRequest.setProductId(PRODUCT_ID);
        itemRequest.setQuantity(1);

        OrderDto.CreateRequest request = new OrderDto.CreateRequest();
        request.setTenantId(TENANT_ID);
        request.setCustomerId(CUSTOMER_ID);
        request.setItems(Collections.singletonList(itemRequest));
        request.setCouponCode(COUPON_CODE);

        Product product = Product.builder()
                .sku("PROD-001")
                .price(new Money(new BigDecimal("100.00"), "USD"))
                .inventoryCount(100)
                .build();
        ReflectionTestUtils.setField(product, "id", PRODUCT_ID);

        when(customerRepository.existsById(CUSTOMER_ID)).thenReturn(true);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(couponRepository.findByCode(COUPON_CODE)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(DomainException.class)
                .hasMessage("Invalid coupon code");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("should throw DomainException when coupon is expired or inactive")
    void testPlaceOrder_ExpiredCoupon() {
        // Arrange
        OrderDto.OrderItemRequest itemRequest = new OrderDto.OrderItemRequest();
        itemRequest.setProductId(PRODUCT_ID);
        itemRequest.setQuantity(1);

        OrderDto.CreateRequest request = new OrderDto.CreateRequest();
        request.setTenantId(TENANT_ID);
        request.setCustomerId(CUSTOMER_ID);
        request.setItems(Collections.singletonList(itemRequest));
        request.setCouponCode(COUPON_CODE);

        Product product = Product.builder()
                .sku("PROD-001")
                .price(new Money(new BigDecimal("100.00"), "USD"))
                .inventoryCount(100)
                .build();
        ReflectionTestUtils.setField(product, "id", PRODUCT_ID);

        Coupon expiredCoupon = Coupon.builder()
                .code(COUPON_CODE)
                .discount(new Money(new BigDecimal("20.00"), "USD"))
                .expiryDate(Instant.now().plusSeconds(604800))
                .active(false)
                .build();
        ReflectionTestUtils.setField(expiredCoupon, "id", UUID.randomUUID());

        when(customerRepository.existsById(CUSTOMER_ID)).thenReturn(true);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(couponRepository.findByCode(COUPON_CODE)).thenReturn(Optional.of(expiredCoupon));

        // Act & Assert
        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(DomainException.class)
                .hasMessage("Coupon is expired or inactive");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("should successfully place order without coupon code")
    void testPlaceOrder_WithoutCoupon() {
        // Arrange
        OrderDto.OrderItemRequest itemRequest = new OrderDto.OrderItemRequest();
        itemRequest.setProductId(PRODUCT_ID);
        itemRequest.setQuantity(1);

        OrderDto.CreateRequest request = new OrderDto.CreateRequest();
        request.setTenantId(TENANT_ID);
        request.setCustomerId(CUSTOMER_ID);
        request.setItems(Collections.singletonList(itemRequest));
        request.setCouponCode(null);

        Product product = Product.builder()
                .sku("PROD-001")
                .price(new Money(new BigDecimal("50.00"), "USD"))
                .inventoryCount(100)
                .build();
        ReflectionTestUtils.setField(product, "id", PRODUCT_ID);

        Order savedOrder = Order.builder()
                .tenantId(TENANT_ID)
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.PLACED)
                .createdAt(Instant.now())
                .totalAmount(new Money(new BigDecimal("50.00"), "USD"))
                .build();
        ReflectionTestUtils.setField(savedOrder, "id", ORDER_ID);

        when(customerRepository.existsById(CUSTOMER_ID)).thenReturn(true);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        OrderDto.Response response = orderService.placeOrder(request);

        // Assert
        assertThat(response).isNotNull();
        verify(couponRepository, never()).findByCode(anyString());
    }

    // ========================= Cancel Order Tests =========================

    @Test
    @DisplayName("should successfully cancel an existing order")
    void testCancelOrder_Success() {
        // Arrange
        Order order = Order.builder()
                .tenantId(TENANT_ID)
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.PLACED)
                .createdAt(Instant.now())
                .build();
        ReflectionTestUtils.setField(order, "id", ORDER_ID);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        orderService.cancelOrder(ORDER_ID);

        // Assert
        verify(orderRepository, times(1)).findById(ORDER_ID);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(eventProducer, times(1)).publish(anyString(), anyString());
    }

    @Test
    @DisplayName("should throw DomainException when canceling non-existent order")
    void testCancelOrder_OrderNotFound() {
        // Arrange
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.cancelOrder(ORDER_ID))
                .isInstanceOf(DomainException.class)
                .hasMessage("Order not found");

        verify(orderRepository, times(1)).findById(ORDER_ID);
        verify(orderRepository, never()).save(any(Order.class));
        verify(eventProducer, never()).publish(anyString(), anyString());
    }

    @Test
    @DisplayName("should publish cancel event after successful order cancellation")
    void testCancelOrder_PublishesEvent() {
        // Arrange
        Order order = Order.builder()
                .status(OrderStatus.PLACED)
                .build();
        ReflectionTestUtils.setField(order, "id", ORDER_ID);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        orderService.cancelOrder(ORDER_ID);

        // Assert
        verify(eventProducer, times(1)).publish("orders.cancelled", "Order cancelled: " + ORDER_ID);
    }

    // ========================= Settle Order Tests =========================

    @Test
    @DisplayName("should successfully settle an existing order")
    void testSettleOrder_Success() {
        // Arrange
        Order order = Order.builder()
                .tenantId(TENANT_ID)
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.PLACED)
                .createdAt(Instant.now())
                .build();
        ReflectionTestUtils.setField(order, "id", ORDER_ID);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        // Act
        orderService.settleOrder(ORDER_ID);

        // Assert
        verify(orderRepository, times(1)).findById(ORDER_ID);
        verify(eventProducer, times(1)).publish(anyString(), anyString());
    }

    @Test
    @DisplayName("should throw DomainException when settling non-existent order")
    void testSettleOrder_OrderNotFound() {
        // Arrange
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.settleOrder(ORDER_ID))
                .isInstanceOf(DomainException.class)
                .hasMessage("Order not found");

        verify(orderRepository, times(1)).findById(ORDER_ID);
        verify(eventProducer, never()).publish(anyString(), anyString());
    }

    @Test
    @DisplayName("should publish settle event after successful order settlement")
    void testSettleOrder_PublishesEvent() {
        // Arrange
        Order order = Order.builder()
                .status(OrderStatus.PLACED)
                .build();
        ReflectionTestUtils.setField(order, "id", ORDER_ID);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        // Act
        orderService.settleOrder(ORDER_ID);

        // Assert
        verify(eventProducer, times(1)).publish("orders.settled", "Order settled: " + ORDER_ID);
    }

    // ========================= Get Order Tests =========================

    @Test
    @DisplayName("should successfully retrieve an order by ID")
    void testGetOrder_Success() {
        // Arrange
        Order order = Order.builder()
                .tenantId(TENANT_ID)
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.PLACED)
                .createdAt(Instant.now())
                .build();
        ReflectionTestUtils.setField(order, "id", ORDER_ID);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        // Act
        OrderDto.Response response = orderService.getOrder(ORDER_ID);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(ORDER_ID);
        assertThat(response.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(response.getStatus()).isEqualTo("PLACED");

        verify(orderRepository, times(1)).findById(ORDER_ID);
    }

    @Test
    @DisplayName("should throw DomainException when retrieving non-existent order")
    void testGetOrder_OrderNotFound() {
        // Arrange
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.getOrder(ORDER_ID))
                .isInstanceOf(DomainException.class)
                .hasMessage("Order not found");

        verify(orderRepository, times(1)).findById(ORDER_ID);
    }

    @Test
    @DisplayName("should correctly map order with items to response DTO")
    void testGetOrder_CorrectMapping() {
        // Arrange
        OrderItem orderItem = OrderItem.builder()
                .productId(PRODUCT_ID)
                .sku("PROD-001")
                .quantity(2)
                .unitPrice(new Money(new BigDecimal("50.00"), "USD"))
                .build();
        ReflectionTestUtils.setField(orderItem, "id", UUID.randomUUID());

        Order order = Order.builder()
                .tenantId(TENANT_ID)
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.PLACED)
                .createdAt(Instant.now())
                .items(Collections.singletonList(orderItem))
                .totalAmount(new Money(new BigDecimal("100.00"), "USD"))
                .build();
        ReflectionTestUtils.setField(order, "id", ORDER_ID);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        // Act
        OrderDto.Response response = orderService.getOrder(ORDER_ID);

        // Assert
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotalAmount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(response.getCurrency()).isEqualTo("USD");

        verify(orderRepository, times(1)).findById(ORDER_ID);
    }

    @Test
    @DisplayName("should handle order with null total amount")
    void testGetOrder_NullTotalAmount() {
        // Arrange
        Order order = Order.builder()
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.PLACED)
                .totalAmount(null)
                .build();
        ReflectionTestUtils.setField(order, "id", ORDER_ID);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        // Act
        OrderDto.Response response = orderService.getOrder(ORDER_ID);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTotalAmount()).isNull();

        verify(orderRepository, times(1)).findById(ORDER_ID);
    }

    @Test
    @DisplayName("should handle order with empty items list")
    void testGetOrder_EmptyItems() {
        // Arrange
        Order order = Order.builder()
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.PLACED)
                .items(Collections.emptyList())
                .build();
        ReflectionTestUtils.setField(order, "id", ORDER_ID);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        // Act
        OrderDto.Response response = orderService.getOrder(ORDER_ID);

        // Assert
        assertThat(response.getItems()).isEmpty();

        verify(orderRepository, times(1)).findById(ORDER_ID);
    }

    @Test
    @DisplayName("should handle order with multiple items and correct currency")
    void testGetOrder_MultipleItemsWithCurrency() {
        // Arrange
        OrderItem item1 = OrderItem.builder()
                .productId(UUID.randomUUID())
                .sku("SKU-001")
                .quantity(1)
                .unitPrice(new Money(new BigDecimal("100.00"), "EUR"))
                .build();
        ReflectionTestUtils.setField(item1, "id", UUID.randomUUID());

        OrderItem item2 = OrderItem.builder()
                .productId(UUID.randomUUID())
                .sku("SKU-002")
                .quantity(2)
                .unitPrice(new Money(new BigDecimal("50.00"), "EUR"))
                .build();
        ReflectionTestUtils.setField(item2, "id", UUID.randomUUID());

        Order order = Order.builder()
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.PLACED)
                .items(Arrays.asList(item1, item2))
                .totalAmount(new Money(new BigDecimal("200.00"), "EUR"))
                .build();
        ReflectionTestUtils.setField(order, "id", ORDER_ID);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        // Act
        OrderDto.Response response = orderService.getOrder(ORDER_ID);

        // Assert
        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getCurrency()).isEqualTo("EUR");
        assertThat(response.getTotalAmount()).isEqualTo(new BigDecimal("200.00"));

        verify(orderRepository, times(1)).findById(ORDER_ID);
    }
}

