package com.best.customer.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.best.customer.dto.CustomerCreateRequest;
import com.best.customer.dto.CustomerPatchRequest;
import com.best.customer.dto.CustomerResponse;
import com.best.customer.entity.Customer;
import com.best.customer.entity.CustomerStatus;
import com.best.customer.exception.CustomerEmailAlreadyExistsException;
import com.best.customer.exception.CustomerNotFoundException;
import com.best.customer.mapper.CustomerMapper;
import com.best.customer.repository.CustomerRepository;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerService(
            CustomerRepository customerRepository,
            CustomerMapper customerMapper) {

        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    // Create a new customer
    public CustomerResponse createCustomer(CustomerCreateRequest request) {

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new CustomerEmailAlreadyExistsException(request.getEmail());
        }

        Customer customer = customerMapper.toEntity(request);

        customer.setStatus(CustomerStatus.ACTIVE);

        Instant now = Instant.now();
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);

        Customer savedCustomer = customerRepository.save(customer);

        return customerMapper.toResponse(savedCustomer);
    }

    // Get a customer by ID
    public CustomerResponse getCustomer(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        return customerMapper.toResponse(customer);
    }

    // Update a customer by ID
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerPatchRequest request) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        if (request.isPresent("firstName")) {
            customer.setFirstName(request.getFirstName());
        }

        if (request.isPresent("lastName")) {
            customer.setLastName(request.getLastName());
        }

        if (request.isPresent("email")) {

            String email = request.getEmail();

            if (customerRepository.existsByEmailAndIdNot(email, id)) {
                throw new CustomerEmailAlreadyExistsException(email);
            }

            customer.setEmail(email);
        }

        if (request.isPresent("phone")) {
            customer.setPhone(request.getPhone());
        }

        customer.setUpdatedAt(Instant.now());

        Customer savedCustomer = customerRepository.save(customer);

        return customerMapper.toResponse(savedCustomer);
    }
}
