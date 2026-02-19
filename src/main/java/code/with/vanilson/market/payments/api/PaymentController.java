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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment & Financials", description = "Endpoints for processing payments, refunds, and transaction tracking")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "List all payments", description = "Retrieves a history of all payment transactions for the tenant.")
    @ApiResponse(responseCode = "200", description = "List of payments retrieved successfully")
    @GetMapping
    public ResponseEntity<List<PaymentDto.Response>> listAllPayments() {
        List<PaymentDto.Response> responses = paymentService.listAllPayments();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get payment by Order ID", description = "Retrieves the payment transaction associated with a specific order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No payment found for the given order")
    })
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentDto.Response> getPaymentByOrderId(
            @PathVariable UUID orderId) {

        PaymentDto.Response response = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Process payment", description = "Executes a payment transaction for an order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment processed successfully"),
            @ApiResponse(responseCode = "400", description = "Payment declined or invalid request")
    })
    @PostMapping
    public ResponseEntity<PaymentDto.Response> processPayment(@RequestBody @Valid PaymentDto.ProcessRequest request) {
        return ResponseEntity.ok(paymentService.processPayment(request));
    }

    @Operation(summary = "Refund order", description = "Initiates a refund for a previously settled order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Refund processed successfully"),
            @ApiResponse(responseCode = "400", description = "Order not eligible for refund")
    })
    @PostMapping("/refund/{orderId}")
    public ResponseEntity<Void> refundOrder(@PathVariable UUID orderId) {
        paymentService.refundOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get payment by ID", description = "Retrieves details of a specific payment transaction.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDto.Response> getPaymentById(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }
}
