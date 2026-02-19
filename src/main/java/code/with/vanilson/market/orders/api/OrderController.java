package code.with.vanilson.market.orders.api;

import code.with.vanilson.market.orders.application.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order & Fulfillment", description = "Endpoints for placing orders and managing fulfillment lifecycle")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Place new order", description = "Creates a new order, allocates inventory, and initiates the fulfillment process.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order placed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid order data or insufficient stock"),
            @ApiResponse(responseCode = "404", description = "Customer or product not found")
    })
    @PostMapping
    public ResponseEntity<OrderDto.Response> placeOrder(@RequestBody @Valid OrderDto.CreateRequest request) {
        return ResponseEntity.ok(orderService.placeOrder(request));
    }

    @Operation(summary = "Get order by ID", description = "Retrieves details and items of a specific order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto.Response> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @Operation(summary = "Cancel order", description = "Cancels a pending order and releases allocated inventory.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Order cannot be cancelled in its current state")
    })
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable UUID id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Settle order", description = "Finalizes the order and marks it as completed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order settled successfully"),
            @ApiResponse(responseCode = "400", description = "Order settlement failed")
    })
    @PostMapping("/{id}/settle")
    public ResponseEntity<Void> settleOrder(@PathVariable UUID id) {
        orderService.settleOrder(id);
        return ResponseEntity.noContent().build();
    }
}
