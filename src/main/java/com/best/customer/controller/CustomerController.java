package com.best.customer.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.best.customer.dto.CustomerCreateRequest;
import com.best.customer.dto.CustomerPatchRequest;
import com.best.customer.dto.CustomerResponse;
import com.best.customer.dto.PageResponse;
import com.best.customer.service.CustomerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // Get a customer by ID
    @GetMapping("/{id}")
    public CustomerResponse getCustomer(@PathVariable Long id) {
        return customerService.getCustomer(id);
    }

    // Get a paginated list of customers
    @GetMapping
    public PageResponse<CustomerResponse> getCustomers(Pageable pageable) {
        return customerService.getCustomers(pageable);
    }

    // Create a new customer
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CustomerCreateRequest request) {

        CustomerResponse response = customerService.createCustomer(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Update a customer by ID
    @PatchMapping("/{id}")
    public CustomerResponse updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerPatchRequest request) {

        return customerService.updateCustomer(id, request);
    }

    // Delete a customer by ID
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
    }
}