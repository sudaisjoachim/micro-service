package com.best.customer.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.best.customer.dto.CustomerCreateRequest;
import com.best.customer.dto.CustomerResponse;
import com.best.customer.entity.CustomerStatus;
import com.best.customer.exception.CustomerNotFoundException;
import com.best.customer.service.CustomerService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.springframework.http.MediaType;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @Test
    void shouldGetCustomerById() throws Exception {

        CustomerResponse response = new CustomerResponse();
        response.setId(1L);
        response.setFirstName("John");
        response.setLastName("Denki");
        response.setEmail("john@denki.com");
        response.setPhone("09012345678");
        response.setStatus(CustomerStatus.ACTIVE);

        when(customerService.getCustomer(1L)).thenReturn(response);

        mockMvc.perform(
                get("/api/v1/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Denki"))
                .andExpect(jsonPath("$.email").value("john@denki.com"))
                .andExpect(jsonPath("$.phone").value("09012345678"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturnNotFoundWhenCustomerDoesNotExist() throws Exception {

        when(customerService.getCustomer(1L)).thenThrow(new CustomerNotFoundException(1L));
        mockMvc.perform(get("/api/v1/customers/1")).andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateCustomer() throws Exception {

        CustomerResponse response = new CustomerResponse();
        response.setId(1L);
        response.setFirstName("John");
        response.setLastName("Denki");
        response.setEmail("john@denki.com");
        response.setPhone("09012345678");
        response.setStatus(CustomerStatus.ACTIVE);

        when(customerService.createCustomer(any(CustomerCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "firstName": "John",
                            "lastName": "Denki",
                            "email": "john@denki.com",
                            "phone": "09012345678"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Denki"))
                .andExpect(jsonPath("$.email").value("john@denki.com"))
                .andExpect(jsonPath("$.phone").value("09012345678"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturnBadRequestWhenCreateRequestIsInvalid() throws Exception {

        mockMvc.perform(
                post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "",
                                    "lastName": "Denki",
                                    "email": "john@denki.com",
                                    "phone": "09012345678"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(customerService, never())
                .createCustomer(any(CustomerCreateRequest.class));
    }
}
