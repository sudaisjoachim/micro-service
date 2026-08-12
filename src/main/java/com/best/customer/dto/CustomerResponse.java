package com.best.customer.dto;

import java.time.Instant;

import com.best.customer.entity.CustomerStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CustomerResponse {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private CustomerStatus status;

    private Instant createdAt;

    private Instant updatedAt;
}