package code.with.vanilson.market.payments.api;

import code.with.vanilson.market.payments.application.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;


    // List all payments
    @GetMapping
    public ResponseEntity<List<PaymentDto.Response>> listAllPayments() {
        List<PaymentDto.Response> responses = paymentService.listAllPayments();
        return ResponseEntity.ok(responses);
    }
    // Get payment by order ID
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentDto.Response> getPaymentByOrderId(
            @PathVariable UUID orderId) {

        PaymentDto.Response response = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<PaymentDto.Response> processPayment(@RequestBody @Valid PaymentDto.ProcessRequest request) {
        return ResponseEntity.ok(paymentService.processPayment(request));
    }

    @PostMapping("/refund/{orderId}")
    public ResponseEntity<Void> refundOrder(@PathVariable UUID orderId) {
        paymentService.refundOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDto.Response> getPaymentById(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }
}
