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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final EventProducer eventProducer;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository, EventProducer eventProducer) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.eventProducer = eventProducer;
    }


    public List<PaymentDto.Response> listAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "paymentByOrderId", key = "#orderId")
    public PaymentDto.Response getPaymentByOrderId(UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order: " + orderId));
        return mapToResponse(payment);
    }


    @Transactional
    public PaymentDto.Response processPayment(PaymentDto.ProcessRequest request) {
        if (!orderRepository.existsById(request.getOrderId())) {
            throw new DomainException("Order not found");
        }

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .amount(new Money(request.getAmount(), request.getCurrency()))
                .status(PaymentStatus.CAPTURED) // Simulating instant capture
                .build();

        payment = paymentRepository.save(payment);

        eventProducer.publish("payments.captured", "Payment captured for order: " + request.getOrderId());

        return mapToResponse(payment);
    }

    @CacheEvict(value = "payments", key = "#orderId")
    @Transactional
    public void refundOrder(java.util.UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order"));

        if (payment.getStatus() != PaymentStatus.CAPTURED) {
            throw new PaymentStatusException("Cannot refund payment in status: " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
        eventProducer.publish("payments.refunded", "Payment refunded for order: " + orderId);
    }

    @Cacheable(value = "payments", key = "#paymentId")
    public PaymentDto.Response getPaymentById(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new PaymentNotFoundException("Payment not found with id: " + paymentId)
                );

        return mapToResponse(payment);
    }


    private PaymentDto.Response mapToResponse(Payment payment) {
        PaymentDto.Response response = new PaymentDto.Response();
        response.setId(payment.getId());
        response.setOrderId(payment.getOrderId());
        response.setAmount(payment.getAmount().getAmount());
        response.setStatus(payment.getStatus().name());
        return response;
    }
}
