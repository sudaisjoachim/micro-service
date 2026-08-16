package com.best.customer.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import java.util.List;

import org.springframework.http.MediaType;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.best.customer.dto.CustomerCreateRequest;
import com.best.customer.dto.CustomerPatchRequest;
import com.best.customer.dto.CustomerResponse;
import com.best.customer.dto.PageResponse;
import com.best.customer.entity.CustomerStatus;
import com.best.customer.exception.CustomerEmailAlreadyExistsException;
import com.best.customer.exception.CustomerNotFoundException;
import com.best.customer.service.CustomerService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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

    @Test
    void shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {

        mockMvc.perform(
                post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "John",
                                    "lastName": "Denki",
                                    "email": "not-an-email",
                                    "phone": "09012345678"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(customerService, never())
                .createCustomer(any(CustomerCreateRequest.class));
    }

    @Test
    void shouldGetCustomers() throws Exception {

        CustomerResponse response = new CustomerResponse();
        response.setId(1L);
        response.setFirstName("John");
        response.setLastName("Denki");
        response.setEmail("john@denki.com");
        response.setStatus(CustomerStatus.ACTIVE);

        PageResponse<CustomerResponse> pageResponse = new PageResponse<>(
                List.of(response),
                0,
                10,
                1,
                1);

        when(customerService.getCustomers(any(Pageable.class))).thenReturn(pageResponse);

        mockMvc.perform(
                get("/api/v1/customers")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].firstName").value("John"))
                .andExpect(jsonPath("$.content[0].email").value("john@denki.com"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void shouldUpdateCustomer() throws Exception {

        CustomerResponse response = new CustomerResponse();
        response.setId(1L);
        response.setFirstName("Updated");
        response.setLastName("Denki");
        response.setEmail("john@denki.com");
        response.setPhone("09012345678");
        response.setStatus(CustomerStatus.ACTIVE);

        when(customerService.updateCustomer(
                eq(1L),
                any(CustomerPatchRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                patch("/api/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "Updated"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.email").value("john@denki.com"));
    }

    @Test
    void shouldReturnBadRequestWhenPatchRequestIsInvalid() throws Exception {

        mockMvc.perform(
                patch("/api/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "not-an-email"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(customerService, never())
                .updateCustomer(
                        eq(1L),
                        any(CustomerPatchRequest.class));
    }

    @Test
    void shouldDeleteCustomer() throws Exception {

        doNothing()
                .when(customerService)
                .deleteCustomer(1L);

        mockMvc.perform(
                delete("/api/v1/customers/1"))
                .andExpect(status().isNoContent());

        verify(customerService)
                .deleteCustomer(1L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingCustomer() throws Exception {

        doThrow(new CustomerNotFoundException(1L))
                .when(customerService)
                .deleteCustomer(1L);

        mockMvc.perform(
                delete("/api/v1/customers/1"))
                .andExpect(status().isNotFound());

        verify(customerService)
                .deleteCustomer(1L);
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {

        when(customerService.createCustomer(any(CustomerCreateRequest.class)))
                .thenThrow(
                        new CustomerEmailAlreadyExistsException(
                                "john@denki.com"));

        mockMvc.perform(
                post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "John",
                                    "lastName": "Denki",
                                    "email": "john@denki.com",
                                    "phone": "09012345678"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnEmptyCustomerPage() throws Exception {

        PageResponse<CustomerResponse> pageResponse = new PageResponse<>(
                List.of(),
                0,
                10,
                0,
                0);

        when(customerService.getCustomers(any(Pageable.class)))
                .thenReturn(pageResponse);

        mockMvc.perform(
                get("/api/v1/customers")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistingCustomer() throws Exception {

        when(customerService.updateCustomer(
                eq(1L),
                any(CustomerPatchRequest.class)))
                .thenThrow(new CustomerNotFoundException(1L));

        mockMvc.perform(
                patch("/api/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "Updated"
                                }
                                """))
                .andExpect(status().isNotFound());

        verify(customerService)
                .updateCustomer(
                        eq(1L),
                        any(CustomerPatchRequest.class));
    }

    @Test
    void shouldPassCustomerIdToService() throws Exception {

        CustomerResponse response = new CustomerResponse();
        response.setId(42L);
        response.setFirstName("John");
        response.setLastName("Denki");
        response.setEmail("john@denki.com");
        response.setStatus(CustomerStatus.ACTIVE);

        when(customerService.getCustomer(42L))
                .thenReturn(response);

        mockMvc.perform(
                get("/api/v1/customers/42"))
                .andExpect(status().isOk());

        verify(customerService)
                .getCustomer(42L);
    }
}
