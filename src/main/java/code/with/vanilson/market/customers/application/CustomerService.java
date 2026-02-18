package code.with.vanilson.market.customers.application;

import code.with.vanilson.market.customers.api.CustomerDto;
import code.with.vanilson.market.customers.domain.Customer;
import code.with.vanilson.market.customers.domain.CustomerRepository;
import code.with.vanilson.market.shared.infrastructure.exception.CustomerAlreadyExistsException;
import code.with.vanilson.market.shared.infrastructure.exception.CustomerNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service

public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }


    @Transactional
    public CustomerDto.Response createCustomer(CustomerDto.CreateRequest request) {
        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomerAlreadyExistsException("Customer with this email already exists");
        }
        Customer customer = Customer.builder()
                .tenantId(request.getTenantId())
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .kycVerified(false)
                .build();

        customer = customerRepository.save(customer);
        return mapToResponse(customer);
    }

    @Cacheable(value = "customers", key = "#id")
    public CustomerDto.Response getCustomerById(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        return mapToResponse(customer);
    }


    @Transactional
    public CustomerDto.Response updateCustomer(UUID id, CustomerDto.UpdateRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        // Optional: check if email is unique
        if (!customer.getEmail().equals(request.getEmail()) &&
                customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomerAlreadyExistsException("Email already in use by another customer");
        }

        // Update fields
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setKycVerified(request.isKycVerified());

        customerRepository.save(customer);

        return mapToResponse(customer);
    }


    @Transactional
    public void verifyKyc(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        customer.verifyKyc();
        customerRepository.save(customer);
    }

    public List<CustomerDto.Response> getAllcustomers() {
        return customerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "customers", key = "#id")
    @Transactional
    public void deleteCustomerById(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        customerRepository.deleteById(customer.getId());
    }

    private CustomerDto.Response mapToResponse(Customer customer) {
        CustomerDto.Response response = new CustomerDto.Response();
        response.setId(customer.getId().toString());
        response.setName(customer.getName());
        response.setEmail(customer.getEmail());
        response.setKycVerified(customer.isKycVerified());
        return response;
    }
}
