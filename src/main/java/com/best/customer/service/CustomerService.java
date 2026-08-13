package com.best.customer.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.best.customer.dto.CustomerCreateRequest;
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

    public CustomerResponse getCustomer(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        return customerMapper.toResponse(customer);
    }
}
