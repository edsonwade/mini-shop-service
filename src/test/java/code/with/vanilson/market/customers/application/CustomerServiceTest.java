package code.with.vanilson.market.customers.application;

import code.with.vanilson.market.customers.api.CustomerDto;
import code.with.vanilson.market.customers.domain.Customer;
import code.with.vanilson.market.customers.domain.CustomerRepository;
import code.with.vanilson.market.shared.infrastructure.exception.CustomerAlreadyExistsException;
import code.with.vanilson.market.shared.infrastructure.exception.CustomerNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@DisplayName("CustomerService Unit Tests")
class CustomerServiceTest {


    private final CustomerRepository customerRepository = mock(CustomerRepository.class);

    private final CustomerService customerService = new CustomerService(customerRepository);

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final String TENANT_ID = UUID.randomUUID().toString();
    private static final String EMAIL = "test@example.com";
    private static final String NAME = "John Doe";
    private static final String PHONE = "+1234567890";

    // ========================= Create Customer Tests =========================

    @Test
    @DisplayName("should successfully create a new customer with valid request")
    void testCreateCustomer_Success() {
        // Arrange
        CustomerDto.CreateRequest request = new CustomerDto.CreateRequest();
        request.setTenantId(TENANT_ID);
        request.setName(NAME);
        request.setEmail(EMAIL);
        request.setPhone(PHONE);

        Customer savedCustomer = Customer.builder()
                .tenantId(TENANT_ID)
                .name(NAME)
                .email(EMAIL)
                .phone(PHONE)
                .kycVerified(false)
                .build();
        ReflectionTestUtils.setField(savedCustomer, "id", CUSTOMER_ID);

        when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        // Act
        CustomerDto.Response response = customerService.createCustomer(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(CUSTOMER_ID.toString());
        assertThat(response.getName()).isEqualTo(NAME);
        assertThat(response.getEmail()).isEqualTo(EMAIL);
        assertThat(response.isKycVerified()).isFalse();

        verify(customerRepository, times(1)).findByEmail(EMAIL);
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("should throw CustomerAlreadyExistsException when email already exists")
    void testCreateCustomer_EmailAlreadyExists() {
        // Arrange
        CustomerDto.CreateRequest request = new CustomerDto.CreateRequest();
        request.setTenantId(TENANT_ID);
        request.setName(NAME);
        request.setEmail(EMAIL);
        request.setPhone(PHONE);

        Customer existingCustomer = Customer.builder()
                .email(EMAIL)
                .build();
        ReflectionTestUtils.setField(existingCustomer, "id", CUSTOMER_ID);

        when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingCustomer));

        // Act & Assert
        assertThatThrownBy(() -> customerService.createCustomer(request))
                .isInstanceOf(CustomerAlreadyExistsException.class)
                .hasMessage("Customer with this email already exists");

        verify(customerRepository, times(1)).findByEmail(EMAIL);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    // ========================= Get Customer By ID Tests =========================

    @Test
    @DisplayName("should successfully retrieve customer by ID")
    void testGetCustomerById_Success() {
        // Arrange
        Customer customer = Customer.builder()

                .tenantId(TENANT_ID)
                .name(NAME)
                .email(EMAIL)
                .phone(PHONE)
                .kycVerified(false)
                .build();
        ReflectionTestUtils.setField(customer, "id", CUSTOMER_ID);

        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));

        // Act
        CustomerDto.Response response = customerService.getCustomerById(CUSTOMER_ID);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(CUSTOMER_ID.toString());
        assertThat(response.getName()).isEqualTo(NAME);
        assertThat(response.getEmail()).isEqualTo(EMAIL);

        verify(customerRepository, times(1)).findById(CUSTOMER_ID);
    }

    @Test
    @DisplayName("should throw CustomerNotFoundException when customer does not exist")
    void testGetCustomerById_CustomerNotFound() {
        // Arrange
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.getCustomerById(CUSTOMER_ID))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessage("Customer not found");

        verify(customerRepository, times(1)).findById(CUSTOMER_ID);
    }

    // ========================= Update Customer Tests =========================

    @Test
    @DisplayName("should successfully update customer with valid data")
    void testUpdateCustomer_Success() {
        // Arrange
        String updatedName = "Jane Doe";
        String updatedEmail = "jane@example.com";
        String updatedPhone = "+9876543210";

        CustomerDto.UpdateRequest request = new CustomerDto.UpdateRequest();
        request.setName(updatedName);
        request.setEmail(updatedEmail);
        request.setPhone(updatedPhone);
        request.setKycVerified(true);

        Customer existingCustomer = Customer.builder()

                .tenantId(TENANT_ID)
                .name(NAME)
                .email(EMAIL)
                .phone(PHONE)
                .kycVerified(false)
                .build();
        ReflectionTestUtils.setField(existingCustomer, "id", CUSTOMER_ID);

        Customer updatedCustomer = Customer.builder()

                .tenantId(TENANT_ID)
                .name(updatedName)
                .email(updatedEmail)
                .phone(updatedPhone)
                .kycVerified(true)
                .build();
        ReflectionTestUtils.setField(updatedCustomer, "id", CUSTOMER_ID);

        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.findByEmail(updatedEmail)).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(updatedCustomer);

        // Act
        CustomerDto.Response response = customerService.updateCustomer(CUSTOMER_ID, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(updatedName);
        assertThat(response.getEmail()).isEqualTo(updatedEmail);
        assertThat(response.isKycVerified()).isTrue();

        verify(customerRepository, times(1)).findById(CUSTOMER_ID);
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("should throw CustomerNotFoundException when updating non-existent customer")
    void testUpdateCustomer_CustomerNotFound() {
        // Arrange
        CustomerDto.UpdateRequest request = new CustomerDto.UpdateRequest();
        request.setName("Updated Name");
        request.setEmail("updated@example.com");
        request.setPhone("+1111111111");
        request.setKycVerified(false);

        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.updateCustomer(CUSTOMER_ID, request))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessage("Customer not found");

        verify(customerRepository, times(1)).findById(CUSTOMER_ID);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("should throw CustomerAlreadyExistsException when updating to duplicate email")
    void testUpdateCustomer_DuplicateEmail() {
        // Arrange
        String existingEmail = "existing@example.com";
        CustomerDto.UpdateRequest request = new CustomerDto.UpdateRequest();
        request.setName("Updated Name");
        request.setEmail(existingEmail);
        request.setPhone("+1111111111");
        request.setKycVerified(false);

        Customer existingCustomer = Customer.builder()

                .email(EMAIL)
                .build();
        ReflectionTestUtils.setField(existingCustomer, "id", CUSTOMER_ID);

        Customer anotherCustomer = Customer.builder()

                .email(existingEmail)
                .build();
        ReflectionTestUtils.setField(anotherCustomer, "id", UUID.randomUUID());

        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.findByEmail(existingEmail)).thenReturn(Optional.of(anotherCustomer));

        // Act & Assert
        assertThatThrownBy(() -> customerService.updateCustomer(CUSTOMER_ID, request))
                .isInstanceOf(CustomerAlreadyExistsException.class)
                .hasMessage("Email already in use by another customer");

        verify(customerRepository, times(1)).findById(CUSTOMER_ID);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("should allow updating same email without throwing exception")
    void testUpdateCustomer_SameEmailNotDuplicate() {
        // Arrange
        CustomerDto.UpdateRequest request = new CustomerDto.UpdateRequest();
        request.setName("Updated Name");
        request.setEmail(EMAIL); // Same email
        request.setPhone("+9876543210");
        request.setKycVerified(true);

        Customer existingCustomer = Customer.builder()

                .name(NAME)
                .email(EMAIL)
                .phone(PHONE)
                .kycVerified(false)
                .build();
        ReflectionTestUtils.setField(existingCustomer, "id", CUSTOMER_ID);

        Customer updatedCustomer = Customer.builder()

                .name("Updated Name")
                .email(EMAIL)
                .phone("+9876543210")
                .kycVerified(true)
                .build();
        ReflectionTestUtils.setField(updatedCustomer, "id", CUSTOMER_ID);

        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(updatedCustomer);

        // Act
        CustomerDto.Response response = customerService.updateCustomer(CUSTOMER_ID, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(EMAIL);

        verify(customerRepository, times(1)).findById(CUSTOMER_ID);
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    // ========================= Verify KYC Tests =========================

    @Test
    @DisplayName("should successfully verify KYC for a customer")
    void testVerifyKyc_Success() {
        // Arrange
        Customer customer = Customer.builder()

                .tenantId(TENANT_ID)
                .name(NAME)
                .email(EMAIL)
                .kycVerified(false)
                .build();
        ReflectionTestUtils.setField(customer, "id", CUSTOMER_ID);

        Customer verifiedCustomer = Customer.builder()

                .tenantId(TENANT_ID)
                .name(NAME)
                .email(EMAIL)
                .kycVerified(true)
                .build();
        ReflectionTestUtils.setField(verifiedCustomer, "id", CUSTOMER_ID);

        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(verifiedCustomer);

        // Act
        customerService.verifyKyc(CUSTOMER_ID);

        // Assert
        verify(customerRepository, times(1)).findById(CUSTOMER_ID);
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("should throw CustomerNotFoundException when verifying KYC for non-existent customer")
    void testVerifyKyc_CustomerNotFound() {
        // Arrange
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.verifyKyc(CUSTOMER_ID))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessage("Customer not found");

        verify(customerRepository, times(1)).findById(CUSTOMER_ID);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    // ========================= Get All Customers Tests =========================

    @Test
    @DisplayName("should successfully retrieve all customers")
    void testGetAllCustomers_Success() {
        // Arrange
        Customer customer1 = Customer.builder()

                .name("Customer 1")
                .email("customer1@example.com")
                .kycVerified(false)
                .build();
        ReflectionTestUtils.setField(customer1, "id", CUSTOMER_ID);

        Customer customer2 = Customer.builder()

                .name("Customer 2")
                .email("customer2@example.com")
                .kycVerified(true)
                .build();
        ReflectionTestUtils.setField(customer2, "id", CUSTOMER_ID);

        List<Customer> customers = Arrays.asList(customer1, customer2);

        when(customerRepository.findAll()).thenReturn(customers);

        // Act
        List<CustomerDto.Response> responses = customerService.getAllcustomers();

        // Assert
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("Customer 1");
        assertThat(responses.get(1).getName()).isEqualTo("Customer 2");

        verify(customerRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("should return empty list when no customers exist")
    void testGetAllCustomers_EmptyList() {
        // Arrange
        when(customerRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<CustomerDto.Response> responses = customerService.getAllcustomers();

        // Assert
        assertThat(responses).isNotNull();
        assertThat(responses).isEmpty();

        verify(customerRepository, times(1)).findAll();
    }

    // ========================= Delete Customer Tests =========================

    @Test
    @DisplayName("should successfully delete a customer by ID")
    void testDeleteCustomerById_Success() {
        // Arrange
        Customer customer = Customer.builder()

                .tenantId(TENANT_ID)
                .name(NAME)
                .email(EMAIL)
                .build();
        ReflectionTestUtils.setField(customer, "id", CUSTOMER_ID);

        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));

        // Act
        customerService.deleteCustomerById(CUSTOMER_ID);

        // Assert
        verify(customerRepository, times(1)).findById(CUSTOMER_ID);
        verify(customerRepository, times(1)).deleteById(CUSTOMER_ID);
    }

    @Test
    @DisplayName("should throw CustomerNotFoundException when deleting non-existent customer")
    void testDeleteCustomerById_CustomerNotFound() {
        // Arrange
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.deleteCustomerById(CUSTOMER_ID))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessage("Customer not found");

        verify(customerRepository, times(1)).findById(CUSTOMER_ID);
        verify(customerRepository, never()).deleteById(any());
    }
}

