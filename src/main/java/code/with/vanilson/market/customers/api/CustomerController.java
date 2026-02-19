package code.with.vanilson.market.customers.api;

import code.with.vanilson.market.customers.application.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
@RequestMapping("/api/customers")
@Tag(name = "Customer & CRM", description = "Endpoints for managing customers and KYC verification")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Operation(summary = "Get all customers", description = "Retrieves a list of all customers indexed in the system for the current tenant.")
    @ApiResponse(responseCode = "200", description = "List of customers retrieved successfully")
    @GetMapping
    public ResponseEntity<List<CustomerDto.Response>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllcustomers());
    }

    @Operation(summary = "Get customer by ID", description = "Retrieves detailed information for a specific customer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto.Response> getCustomerById(@PathVariable UUID id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @Operation(summary = "Create new customer", description = "Onboards a new customer into the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid customer data")
    })
    @PostMapping
    public ResponseEntity<CustomerDto.Response> createCustomer(@RequestBody @Valid CustomerDto.CreateRequest request) {
        return ResponseEntity.ok(customerService.createCustomer(request));
    }

    @Operation(summary = "Update customer", description = "Updates an existing customer's profile information.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer updated successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomerDto.Response> updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerDto.UpdateRequest request) {

        CustomerDto.Response updatedCustomer = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(updatedCustomer);
    }

    @Operation(summary = "Verify KYC", description = "Marks a customer's KYC status as verified.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "KYC verified successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PostMapping("/{id}/kyc")
    public ResponseEntity<Void> verifyKyc(@PathVariable UUID id) {
        customerService.verifyKyc(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete customer", description = "Performs a hard delete of a customer record.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Customer deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable UUID id) {
        customerService.deleteCustomerById(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
