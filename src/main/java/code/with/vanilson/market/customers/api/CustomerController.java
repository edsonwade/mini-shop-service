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

@RestController
@RequestMapping("/api/customers")

public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }


    @GetMapping
    public ResponseEntity<List<CustomerDto.Response>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllcustomers());
    }


    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto.Response> getCustomerById(@PathVariable UUID id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PostMapping
    public ResponseEntity<CustomerDto.Response> createCustomer(@RequestBody @Valid CustomerDto.CreateRequest request) {
        return ResponseEntity.ok(customerService.createCustomer(request));
    }


    // Update customer
    @PutMapping("/{id}")
    public ResponseEntity<CustomerDto.Response> updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerDto.UpdateRequest request) {

        CustomerDto.Response updatedCustomer = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(updatedCustomer);
    }


    @PostMapping("/{id}/kyc")
    public ResponseEntity<Void> verifyKyc(@PathVariable UUID id) {
        customerService.verifyKyc(id);
        return ResponseEntity.noContent().build();
    }


    // Delete customer by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable UUID id) {
        customerService.deleteCustomerById(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
