package com.best.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.best.customer.dto.CustomerCreateRequest;
import com.best.customer.dto.CustomerPatchRequest;
import com.best.customer.dto.CustomerResponse;
import com.best.customer.dto.PageResponse;
import com.best.customer.entity.Customer;
import com.best.customer.entity.CustomerStatus;
import com.best.customer.exception.CustomerEmailAlreadyExistsException;
import com.best.customer.exception.CustomerNotFoundException;
import com.best.customer.mapper.CustomerMapper;
import com.best.customer.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    // mock the behavior of the repository and mapper to test the service layer
    @Test
    void shouldCreateCustomer() {

        CustomerCreateRequest request = new CustomerCreateRequest();
        request.setFirstName("John");
        request.setLastName("Denki");
        request.setEmail("john@denki.com");
        request.setPhone("09012345678");

        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Denki");
        customer.setEmail("john@denki.com");
        customer.setPhone("09012345678");

        Customer savedCustomer = new Customer();
        savedCustomer.setId(1L);
        savedCustomer.setFirstName("John");
        savedCustomer.setLastName("Denki");
        savedCustomer.setEmail("john@denki.com");
        savedCustomer.setPhone("09012345678");
        savedCustomer.setStatus(CustomerStatus.ACTIVE);

        CustomerResponse response = new CustomerResponse();
        response.setId(1L);
        response.setFirstName("John");
        response.setLastName("Denki");
        response.setEmail("john@denki.com");
        response.setPhone("09012345678");
        response.setStatus(CustomerStatus.ACTIVE);

        when(customerRepository.existsByEmail("john@denki.com"))
                .thenReturn(false);

        when(customerMapper.toEntity(request))
                .thenReturn(customer);

        when(customerRepository.save(customer))
                .thenReturn(savedCustomer);

        when(customerMapper.toResponse(savedCustomer))
                .thenReturn(response);

        CustomerResponse result = customerService.createCustomer(request);

        assertThat(result).isSameAs(response);
        assertThat(result.getEmail()).isEqualTo("john@denki.com");
        assertThat(result.getStatus()).isEqualTo(CustomerStatus.ACTIVE);

        verify(customerRepository)
                .existsByEmail("john@denki.com");

        verify(customerRepository)
                .save(customer);

        verify(customerMapper)
                .toEntity(request);

        verify(customerMapper)
                .toResponse(savedCustomer);
    }

    // Test to check if the service throws an exception when trying to create a
    // customer with an existing email
    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {

        CustomerCreateRequest request = new CustomerCreateRequest();
        request.setFirstName("John");
        request.setLastName("Denki");
        request.setEmail("john@denki.com");
        request.setPhone("09012345678");

        // if the email already exists, the service should throw a
        // CustomerEmailAlreadyExistsException ,no need to call the repository's save
        // method or the mapper's toEntity method
        when(customerRepository.existsByEmail("john@denki.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> customerService.createCustomer(request))
                .isInstanceOf(CustomerEmailAlreadyExistsException.class);

        verify(customerRepository).existsByEmail("john@denki.com");

        verify(customerRepository, never()).save(any(Customer.class));

        verify(customerMapper, never()).toEntity(any(CustomerCreateRequest.class));
    }

    @Test
    void shouldSetCustomerStatusToActiveBeforeSaving() {

        CustomerCreateRequest request = new CustomerCreateRequest();
        request.setFirstName("John");
        request.setLastName("Denki");
        request.setEmail("john@denki.com");
        request.setPhone("09012345678");

        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Denki");
        customer.setEmail("john@denki.com");
        customer.setPhone("09012345678");

        CustomerResponse response = new CustomerResponse();
        response.setEmail("john@denki.com");
        response.setStatus(CustomerStatus.ACTIVE);

        when(customerRepository.existsByEmail("john@denki.com"))
                .thenReturn(false);

        when(customerMapper.toEntity(request))
                .thenReturn(customer);

        when(customerRepository.save(any(Customer.class)))
                .thenReturn(customer);

        when(customerMapper.toResponse(customer))
                .thenReturn(response);

        customerService.createCustomer(request);

        verify(customerRepository).save(
                argThat(savedCustomer -> savedCustomer.getStatus() == CustomerStatus.ACTIVE));
    }

    // Test to check if the service can retrieve a customer by ID
    @Test
    void shouldGetCustomerById() {

        Long id = 1L;

        Customer customer = new Customer();
        customer.setId(id);
        customer.setFirstName("John");
        customer.setLastName("Denki");
        customer.setEmail("john@denki.com");
        customer.setPhone("09012345678");
        customer.setStatus(CustomerStatus.ACTIVE);

        CustomerResponse response = new CustomerResponse();
        response.setId(id);
        response.setFirstName("John");
        response.setLastName("Denki");
        response.setEmail("john@denki.com");
        response.setPhone("09012345678");
        response.setStatus(CustomerStatus.ACTIVE);

        when(customerRepository.findById(id))
                .thenReturn(Optional.of(customer));

        when(customerMapper.toResponse(customer))
                .thenReturn(response);

        CustomerResponse result = customerService.getCustomer(id);

        assertThat(result).isSameAs(response);

        verify(customerRepository)
                .findById(id);

        verify(customerMapper)
                .toResponse(customer);
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFound() {

        Long id = 1L;

        when(customerRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomer(id))
                .isInstanceOf(CustomerNotFoundException.class);

        verify(customerRepository)
                .findById(id);

        verify(customerMapper, never())
                .toResponse(any(Customer.class));
    }

    @Test
    void shouldUpdateFirstName() {

        Long id = 1L;

        Customer customer = new Customer();
        customer.setId(id);
        customer.setFirstName("John");
        customer.setLastName("Denki");
        customer.setEmail("john@denki.com");
        customer.setPhone("09012345678");
        customer.setStatus(CustomerStatus.ACTIVE);

        CustomerPatchRequest request = new CustomerPatchRequest();
        request.setFirstName("Updated");

        CustomerResponse response = new CustomerResponse();
        response.setId(id);
        response.setFirstName("Updated");
        response.setLastName("Denki");
        response.setEmail("john@denki.com");
        response.setPhone("09012345678");
        response.setStatus(CustomerStatus.ACTIVE);

        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
        when(customerRepository.save(customer)).thenReturn(customer);
        when(customerMapper.toResponse(customer)).thenReturn(response);

        CustomerResponse result = customerService.updateCustomer(id, request);

        assertThat(customer.getFirstName()).isEqualTo("Updated");
        assertThat(result.getFirstName()).isEqualTo("Updated");
        verify(customerRepository).findById(id);
        verify(customerRepository).save(customer);
        verify(customerMapper).toResponse(customer);
    }

    @Test
    void shouldUpdateLastName() {

        Long id = 1L;

        Customer customer = new Customer();
        customer.setId(id);
        customer.setFirstName("John");
        customer.setLastName("Denki");
        customer.setEmail("john@denki.com");
        customer.setPhone("09012345678");
        customer.setStatus(CustomerStatus.ACTIVE);

        CustomerPatchRequest request = new CustomerPatchRequest();
        request.setLastName("Updated");

        CustomerResponse response = new CustomerResponse();
        response.setId(id);
        response.setFirstName("John");
        response.setLastName("Updated");
        response.setEmail("john@denki.com");
        response.setPhone("09012345678");
        response.setStatus(CustomerStatus.ACTIVE);

        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
        when(customerRepository.save(customer)).thenReturn(customer);
        when(customerMapper.toResponse(customer)).thenReturn(response);

        CustomerResponse result = customerService.updateCustomer(id, request);

        assertThat(customer.getLastName()).isEqualTo("Updated");
        assertThat(result.getLastName()).isEqualTo("Updated");
        verify(customerRepository).findById(id);
        verify(customerRepository).save(customer);
        verify(customerMapper).toResponse(customer);
    }

    @Test
    void shouldUpdatePhone() {

        Long id = 1L;

        Customer customer = new Customer();
        customer.setId(id);
        customer.setFirstName("John");
        customer.setLastName("Denki");
        customer.setEmail("john@denki.com");
        customer.setPhone("09012345678");
        customer.setStatus(CustomerStatus.ACTIVE);

        CustomerPatchRequest request = new CustomerPatchRequest();
        request.setPhone("09099999999");

        CustomerResponse response = new CustomerResponse();
        response.setId(id);
        response.setFirstName("John");
        response.setLastName("Denki");
        response.setEmail("john@denki.com");
        response.setPhone("09099999999");
        response.setStatus(CustomerStatus.ACTIVE);

        when(customerRepository.findById(id))
                .thenReturn(Optional.of(customer));

        when(customerRepository.save(customer))
                .thenReturn(customer);

        when(customerMapper.toResponse(customer))
                .thenReturn(response);

        CustomerResponse result = customerService.updateCustomer(id, request);

        assertThat(customer.getPhone())
                .isEqualTo("09099999999");

        assertThat(result.getPhone())
                .isEqualTo("09099999999");

        verify(customerRepository)
                .findById(id);

        verify(customerRepository)
                .save(customer);

        verify(customerMapper)
                .toResponse(customer);
    }

    @Test
    void shouldUpdateEmail() {

        Long id = 1L;

        Customer customer = new Customer();
        customer.setId(id);
        customer.setFirstName("John");
        customer.setLastName("Denki");
        customer.setEmail("john@denki.com");
        customer.setPhone("09012345678");
        customer.setStatus(CustomerStatus.ACTIVE);

        CustomerPatchRequest request = new CustomerPatchRequest();
        request.setEmail("new@denki.com");

        CustomerResponse response = new CustomerResponse();
        response.setId(id);
        response.setEmail("new@denki.com");
        response.setStatus(CustomerStatus.ACTIVE);

        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
        when(customerRepository.existsByEmailAndIdNot("new@denki.com", id)).thenReturn(false);
        when(customerRepository.save(customer)).thenReturn(customer);
        when(customerMapper.toResponse(customer)).thenReturn(response);
        CustomerResponse result = customerService.updateCustomer(id, request);

        assertThat(customer.getEmail()).isEqualTo("new@denki.com");
        assertThat(result.getEmail()).isEqualTo("new@denki.com");
        verify(customerRepository).findById(id);
        verify(customerRepository).existsByEmailAndIdNot("new@denki.com", id);
        verify(customerRepository).save(customer);
        verify(customerMapper).toResponse(customer);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingToExistingEmail() {

        Long id = 1L;
        Customer customer = new Customer();
        customer.setId(id);
        customer.setFirstName("John");
        customer.setLastName("Denki");
        customer.setEmail("john@denki.com");
        customer.setPhone("09012345678");
        customer.setStatus(CustomerStatus.ACTIVE);

        CustomerPatchRequest request = new CustomerPatchRequest();
        request.setEmail("other@denki.com");

        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
        when(customerRepository.existsByEmailAndIdNot("other@denki.com", id)).thenReturn(true);
        assertThatThrownBy(() -> customerService.updateCustomer(id, request))
                .isInstanceOf(CustomerEmailAlreadyExistsException.class);

        verify(customerRepository).findById(id);
        verify(customerRepository).existsByEmailAndIdNot("other@denki.com", id);
        verify(customerRepository, never()).save(any(Customer.class));
        verify(customerMapper, never()).toResponse(any(Customer.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingCustomer() {

        Long id = 1L;

        CustomerPatchRequest request = new CustomerPatchRequest();
        request.setFirstName("Updated");

        when(customerRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> customerService.updateCustomer(id, request))
                .isInstanceOf(CustomerNotFoundException.class);

        verify(customerRepository).findById(id);
        verify(customerRepository, never()).save(any(Customer.class));
        verify(customerMapper, never()).toResponse(any(Customer.class));
    }

    @Test
    void shouldDeleteCustomer() {

        Long id = 1L;

        Customer customer = new Customer();
        customer.setId(id);
        customer.setFirstName("John");
        customer.setLastName("Denki");
        customer.setEmail("john@denki.com");

        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
        customerService.deleteCustomer(id);
        verify(customerRepository).findById(id);
        verify(customerRepository).delete(customer);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingCustomer() {

        Long id = 1L;
        when(customerRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> customerService.deleteCustomer(id)).isInstanceOf(CustomerNotFoundException.class);
        verify(customerRepository).findById(id);
        verify(customerRepository, never()).delete(any(Customer.class));
    }

    @Test
    void shouldGetCustomers() {

        Pageable pageable = PageRequest.of(0, 10);

        Customer customer1 = new Customer();
        customer1.setId(1L);
        customer1.setFirstName("John");
        customer1.setLastName("Denki");
        customer1.setEmail("john@denki.com");
        customer1.setStatus(CustomerStatus.ACTIVE);

        Customer customer2 = new Customer();
        customer2.setId(2L);
        customer2.setFirstName("Jane");
        customer2.setLastName("Denki");
        customer2.setEmail("jane@denki.com");
        customer2.setStatus(CustomerStatus.ACTIVE);

        CustomerResponse response1 = new CustomerResponse();
        response1.setId(1L);
        response1.setFirstName("John");
        response1.setEmail("john@denki.com");

        CustomerResponse response2 = new CustomerResponse();
        response2.setId(2L);
        response2.setFirstName("Jane");
        response2.setEmail("jane@denki.com");

        Page<Customer> page = new PageImpl<>(List.of(customer1, customer2), pageable, 2);

        when(customerRepository.findAll(pageable)).thenReturn(page);
        when(customerMapper.toResponse(customer1)).thenReturn(response1);
        when(customerMapper.toResponse(customer2)).thenReturn(response2);

        PageResponse<CustomerResponse> result = customerService.getCustomers(pageable);

        assertThat(result.content()).containsExactly(response1, response2);
        assertThat(result.page()).isEqualTo(0);
        assertThat(result.size()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.totalPages()).isEqualTo(1);
        verify(customerRepository).findAll(pageable);
        verify(customerMapper).toResponse(customer1);
        verify(customerMapper).toResponse(customer2);
    }

    @Test
    void shouldReturnEmptyPageWhenNoCustomersFound() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(customerRepository.findAll(pageable)).thenReturn(emptyPage);

        PageResponse<CustomerResponse> result = customerService.getCustomers(pageable);
        assertThat(result.content()).isEmpty();
        assertThat(result.page()).isEqualTo(0);
        assertThat(result.size()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(0);
        assertThat(result.totalPages()).isEqualTo(0);
        verify(customerRepository).findAll(pageable);
        verify(customerMapper, never()).toResponse(any(Customer.class));
    }

}
