package code.with.vanilson.market.payments.application;

import code.with.vanilson.market.events.domain.EventProducer;
import code.with.vanilson.market.orders.domain.OrderRepository;
import code.with.vanilson.market.payments.api.PaymentDto;
import code.with.vanilson.market.payments.domain.Payment;
import code.with.vanilson.market.payments.domain.PaymentRepository;
import code.with.vanilson.market.payments.domain.PaymentStatus;
import code.with.vanilson.market.shared.domain.DomainException;
import code.with.vanilson.market.shared.domain.Money;
import code.with.vanilson.market.shared.infrastructure.exception.PaymentNotFoundException;
import code.with.vanilson.market.shared.infrastructure.exception.PaymentStatusException;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Unit Tests")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private EventProducer eventProducer;

    @InjectMocks
    private PaymentService paymentService;

    private static final UUID PAYMENT_ID = UUID.randomUUID();
    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final BigDecimal AMOUNT = new BigDecimal("99.99");
    private static final String CURRENCY = "USD";

    // ========================= List All Payments Tests =========================

    @Test
    @DisplayName("should successfully retrieve all payments")
    void testListAllPayments_Success() {
        // Arrange
        Payment payment1 = Payment.builder()
                .orderId(UUID.randomUUID())
                .amount(new Money(new BigDecimal("50.00"), "USD"))
                .status(PaymentStatus.CAPTURED)
                .build();
        ReflectionTestUtils.setField(payment1, "id", PAYMENT_ID);

        Payment payment2 = Payment.builder()

                .orderId(UUID.randomUUID())
                .amount(new Money(new BigDecimal("75.00"), "USD"))
                .status(PaymentStatus.CAPTURED)
                .build();
        ReflectionTestUtils.setField(payment2, "id", PAYMENT_ID);

        List<Payment> payments = Arrays.asList(payment1, payment2);

        when(paymentRepository.findAll()).thenReturn(payments);

        // Act
        List<PaymentDto.Response> responses = paymentService.listAllPayments();

        // Assert
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(2);
        assertThat(responses).extracting("status")
                .containsExactly("CAPTURED", "CAPTURED");

        verify(paymentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("should return empty list when no payments exist")
    void testListAllPayments_EmptyList() {
        // Arrange
        when(paymentRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<PaymentDto.Response> responses = paymentService.listAllPayments();

        // Assert
        assertThat(responses).isNotNull();
        assertThat(responses).isEmpty();

        verify(paymentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("should correctly map all payments in response")
    void testListAllPayments_CorrectMapping() {
        // Arrange
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();

        Payment payment1 = Payment.builder()
                .orderId(orderId1)
                .amount(new Money(new BigDecimal("100.00"), "EUR"))
                .status(PaymentStatus.CAPTURED)
                .build();
        ReflectionTestUtils.setField(payment1, "id", PAYMENT_ID);

        Payment payment2 = Payment.builder()
                .orderId(orderId2)
                .amount(new Money(new BigDecimal("200.00"), "GBP"))
                .status(PaymentStatus.REFUNDED)
                .build();
        ReflectionTestUtils.setField(payment2, "id", PAYMENT_ID);

        when(paymentRepository.findAll()).thenReturn(Arrays.asList(payment1, payment2));

        // Act
        List<PaymentDto.Response> responses = paymentService.listAllPayments();

        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getAmount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(responses.get(1).getAmount()).isEqualTo(new BigDecimal("200.00"));

        verify(paymentRepository, times(1)).findAll();
    }

    // ========================= Get Payment By Order ID Tests =========================

    @Test
    @DisplayName("should successfully retrieve payment by order ID")
    void testGetPaymentByOrderId_Success() {
        // Arrange
        Payment payment = Payment.builder()
                .orderId(ORDER_ID)
                .amount(new Money(AMOUNT, CURRENCY))
                .status(PaymentStatus.CAPTURED)
                .build();
        ReflectionTestUtils.setField(payment, "id", PAYMENT_ID);

        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));

        // Act
        PaymentDto.Response response = paymentService.getPaymentByOrderId(ORDER_ID);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(PAYMENT_ID);
        assertThat(response.getOrderId()).isEqualTo(ORDER_ID);
        assertThat(response.getAmount()).isEqualTo(AMOUNT);
        assertThat(response.getStatus()).isEqualTo("CAPTURED");

        verify(paymentRepository, times(1)).findByOrderId(ORDER_ID);
    }

    @Test
    @DisplayName("should throw PaymentNotFoundException when payment not found by order ID")
    void testGetPaymentByOrderId_NotFound() {
        // Arrange
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> paymentService.getPaymentByOrderId(ORDER_ID))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessage("Payment not found for order: " + ORDER_ID);

        verify(paymentRepository, times(1)).findByOrderId(ORDER_ID);
    }

    // ========================= Process Payment Tests =========================

    @Test
    @DisplayName("should successfully process payment for existing order")
    void testProcessPayment_Success() {
        // Arrange
        PaymentDto.ProcessRequest request = new PaymentDto.ProcessRequest();
        request.setOrderId(ORDER_ID);
        request.setAmount(AMOUNT);
        request.setCurrency(CURRENCY);

        Payment savedPayment = Payment.builder()
                .orderId(ORDER_ID)
                .amount(new Money(AMOUNT, CURRENCY))
                .status(PaymentStatus.CAPTURED)
                .build();
        ReflectionTestUtils.setField(savedPayment, "id", PAYMENT_ID);

        when(orderRepository.existsById(ORDER_ID)).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // Act
        PaymentDto.Response response = paymentService.processPayment(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(PAYMENT_ID);
        assertThat(response.getOrderId()).isEqualTo(ORDER_ID);
        assertThat(response.getStatus()).isEqualTo("CAPTURED");

        verify(orderRepository, times(1)).existsById(ORDER_ID);
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(eventProducer, times(1)).publish(anyString(), anyString());
    }

    @Test
    @DisplayName("should throw DomainException when order not found during payment processing")
    void testProcessPayment_OrderNotFound() {
        // Arrange
        PaymentDto.ProcessRequest request = new PaymentDto.ProcessRequest();
        request.setOrderId(ORDER_ID);
        request.setAmount(AMOUNT);
        request.setCurrency(CURRENCY);

        when(orderRepository.existsById(ORDER_ID)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> paymentService.processPayment(request))
                .isInstanceOf(DomainException.class)
                .hasMessage("Order not found");

        verify(orderRepository, times(1)).existsById(ORDER_ID);
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(eventProducer, never()).publish(anyString(), anyString());
    }

    @Test
    @DisplayName("should publish event after successful payment processing")
    void testProcessPayment_PublishesEvent() {
        // Arrange
        PaymentDto.ProcessRequest request = new PaymentDto.ProcessRequest();
        request.setOrderId(ORDER_ID);
        request.setAmount(AMOUNT);
        request.setCurrency(CURRENCY);

        Payment savedPayment = Payment.builder()
                .orderId(ORDER_ID)
                .amount(new Money(AMOUNT, CURRENCY))
                .status(PaymentStatus.CAPTURED)
                .build();
        ReflectionTestUtils.setField(savedPayment, "id", PAYMENT_ID);

        when(orderRepository.existsById(ORDER_ID)).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // Act
        paymentService.processPayment(request);

        // Assert
        verify(eventProducer, times(1)).publish("payments.captured", "Payment captured for order: " + ORDER_ID);
    }

    @Test
    @DisplayName("should process payment with zero amount")
    void testProcessPayment_ZeroAmount() {
        // Arrange
        PaymentDto.ProcessRequest request = new PaymentDto.ProcessRequest();
        request.setOrderId(ORDER_ID);
        request.setAmount(BigDecimal.ZERO);
        request.setCurrency(CURRENCY);

        Payment savedPayment = Payment.builder()

                .orderId(ORDER_ID)
                .amount(new Money(BigDecimal.ZERO, CURRENCY))
                .status(PaymentStatus.CAPTURED)
                .build();
        ReflectionTestUtils.setField(savedPayment, "id", PAYMENT_ID);

        when(orderRepository.existsById(ORDER_ID)).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // Act
        PaymentDto.Response response = paymentService.processPayment(request);

        // Assert
        assertThat(response.getAmount()).isEqualTo(new BigDecimal("0.00"));
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    // ========================= Refund Order Tests =========================

    @Test
    @DisplayName("should successfully refund a captured payment")
    void testRefundOrder_Success() {
        // Arrange
        Payment payment = Payment.builder()

                .orderId(ORDER_ID)
                .amount(new Money(AMOUNT, CURRENCY))
                .status(PaymentStatus.CAPTURED)
                .build();
        ReflectionTestUtils.setField(payment, "id", PAYMENT_ID);

        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // Act
        paymentService.refundOrder(ORDER_ID);

        // Assert
        verify(paymentRepository, times(1)).findByOrderId(ORDER_ID);
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(eventProducer, times(1)).publish("payments.refunded", "Payment refunded for order: " + ORDER_ID);
    }

    @Test
    @DisplayName("should throw PaymentNotFoundException when refunding non-existent payment")
    void testRefundOrder_PaymentNotFound() {
        // Arrange
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> paymentService.refundOrder(ORDER_ID))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessage("Payment not found for order");

        verify(paymentRepository, times(1)).findByOrderId(ORDER_ID);
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(eventProducer, never()).publish(anyString(), anyString());
    }

    @Test
    @DisplayName("should throw PaymentStatusException when refunding non-captured payment")
    void testRefundOrder_InvalidStatus() {
        // Arrange
        Payment payment = Payment.builder()

                .orderId(ORDER_ID)
                .amount(new Money(AMOUNT, CURRENCY))
                .status(PaymentStatus.REFUNDED)
                .build();
        ReflectionTestUtils.setField(payment, "id", PAYMENT_ID);

        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));

        // Act & Assert
        assertThatThrownBy(() -> paymentService.refundOrder(ORDER_ID))
                .isInstanceOf(PaymentStatusException.class)
                .hasMessage("Cannot refund payment in status: " + PaymentStatus.REFUNDED);

        verify(paymentRepository, times(1)).findByOrderId(ORDER_ID);
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(eventProducer, never()).publish(anyString(), anyString());
    }

    @Test
    @DisplayName("should change payment status to REFUNDED after successful refund")
    void testRefundOrder_StatusChange() {
        // Arrange
        Payment payment = Payment.builder()

                .orderId(ORDER_ID)
                .amount(new Money(AMOUNT, CURRENCY))
                .status(PaymentStatus.CAPTURED)
                .build();
        ReflectionTestUtils.setField(payment, "id", PAYMENT_ID);

        Payment refundedPayment = Payment.builder()

                .orderId(ORDER_ID)
                .amount(new Money(AMOUNT, CURRENCY))
                .status(PaymentStatus.REFUNDED)
                .build();

        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(refundedPayment);

        // Act
        paymentService.refundOrder(ORDER_ID);

        // Assert
        verify(paymentRepository).save(argThat(p -> p.getStatus() == PaymentStatus.REFUNDED));
    }

    // ========================= Get Payment By ID Tests =========================

    @Test
    @DisplayName("should successfully retrieve payment by payment ID")
    void testGetPaymentById_Success() {
        // Arrange
        Payment payment = Payment.builder()

                .orderId(ORDER_ID)
                .amount(new Money(AMOUNT, CURRENCY))
                .status(PaymentStatus.CAPTURED)
                .build();
        ReflectionTestUtils.setField(payment, "id", PAYMENT_ID);

        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));

        // Act
        PaymentDto.Response response = paymentService.getPaymentById(PAYMENT_ID);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(PAYMENT_ID);
        assertThat(response.getOrderId()).isEqualTo(ORDER_ID);
        assertThat(response.getAmount()).isEqualTo(AMOUNT);
        assertThat(response.getStatus()).isEqualTo("CAPTURED");

        verify(paymentRepository, times(1)).findById(PAYMENT_ID);
    }

    @Test
    @DisplayName("should throw PaymentNotFoundException when payment ID not found")
    void testGetPaymentById_NotFound() {
        // Arrange
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> paymentService.getPaymentById(PAYMENT_ID))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessage("Payment not found with id: " + PAYMENT_ID);

        verify(paymentRepository, times(1)).findById(PAYMENT_ID);
    }

    @Test
    @DisplayName("should correctly map payment to response DTO by ID")
    void testGetPaymentById_CorrectMapping() {
        // Arrange
        Payment payment = Payment.builder()

                .orderId(ORDER_ID)
                .amount(new Money(new BigDecimal("149.50"), "EUR"))
                .status(PaymentStatus.CAPTURED)
                .build();
        ReflectionTestUtils.setField(payment, "id", PAYMENT_ID);

        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));

        // Act
        PaymentDto.Response response = paymentService.getPaymentById(PAYMENT_ID);

        // Assert
        assertThat(response.getAmount()).isEqualTo(new BigDecimal("149.50"));
        assertThat(response.getStatus()).isEqualTo("CAPTURED");

        verify(paymentRepository, times(1)).findById(PAYMENT_ID);
    }

    // ========================= Edge Cases and Exception Handling Tests =========================

    @Test
    @DisplayName("should handle multiple payments with different statuses")
    void testListAllPayments_DifferentStatuses() {
        // Arrange
        Payment payment1 = Payment.builder()

                .orderId(UUID.randomUUID())
                .amount(new Money(new BigDecimal("50.00"), "USD"))
                .status(PaymentStatus.CAPTURED)
                .build();
        ReflectionTestUtils.setField(payment1, "id", PAYMENT_ID);

        Payment payment2 = Payment.builder()

                .orderId(UUID.randomUUID())
                .amount(new Money(new BigDecimal("75.00"), "USD"))
                .status(PaymentStatus.REFUNDED)
                .build();
        ReflectionTestUtils.setField(payment2, "id", PAYMENT_ID);

        when(paymentRepository.findAll()).thenReturn(Arrays.asList(payment1, payment2));

        // Act
        List<PaymentDto.Response> responses = paymentService.listAllPayments();

        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses).extracting("status")
                .containsExactly("CAPTURED", "REFUNDED");

        verify(paymentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("should process payment with large amount")
    void testProcessPayment_LargeAmount() {
        // Arrange
        BigDecimal largeAmount = new BigDecimal("9999999.99");
        PaymentDto.ProcessRequest request = new PaymentDto.ProcessRequest();
        request.setOrderId(ORDER_ID);
        request.setAmount(largeAmount);
        request.setCurrency(CURRENCY);

        Payment savedPayment = Payment.builder()

                .orderId(ORDER_ID)
                .amount(new Money(largeAmount, CURRENCY))
                .status(PaymentStatus.CAPTURED)
                .build();
        ReflectionTestUtils.setField(savedPayment, "id", PAYMENT_ID);

        when(orderRepository.existsById(ORDER_ID)).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // Act
        PaymentDto.Response response = paymentService.processPayment(request);

        // Assert
        assertThat(response.getAmount()).isEqualTo(largeAmount);

        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("should handle payments with different currencies")
    void testGetPaymentById_DifferentCurrencies() {
        // Arrange
        UUID paymentIdEUR = UUID.randomUUID();
        Payment paymentEUR = Payment.builder()
                .orderId(ORDER_ID)
                .amount(new Money(new BigDecimal("50.00"), "EUR"))
                .status(PaymentStatus.CAPTURED)
                .build();
        ReflectionTestUtils.setField(paymentEUR, "id", paymentIdEUR);

        when(paymentRepository.findById(paymentIdEUR)).thenReturn(Optional.of(paymentEUR));

        // Act
        PaymentDto.Response response = paymentService.getPaymentById(paymentIdEUR);

        // Assert
        assertThat(response.getAmount()).isEqualTo(new BigDecimal("50.00"));

        verify(paymentRepository, times(1)).findById(paymentIdEUR);
    }

    @Test
    @DisplayName("should verify repository interactions during refund")
    void testRefundOrder_VerifyRepositoryInteractions() {
        // Arrange
        Payment payment = Payment.builder()

                .orderId(ORDER_ID)
                .status(PaymentStatus.CAPTURED)
                .build();
        ReflectionTestUtils.setField(payment, "id", PAYMENT_ID);

        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // Act
        paymentService.refundOrder(ORDER_ID);

        // Assert
        verify(paymentRepository).findByOrderId(ORDER_ID);
        verify(paymentRepository).save(any(Payment.class));
        verify(eventProducer).publish(anyString(), anyString());
        verifyNoMoreInteractions(paymentRepository);
    }
}

