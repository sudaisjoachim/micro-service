package com.best.customer.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import org.springframework.http.MediaType;

import com.best.customer.entity.Customer;
import com.best.customer.entity.CustomerStatus;
import com.best.customer.repository.CustomerRepository;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.BeforeEach;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class CustomerIntegrationTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("customer_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {

        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl);

        registry.add(
                "spring.datasource.username",
                postgres::getUsername);

        registry.add(
                "spring.datasource.password",
                postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void cleanDatabase() {
        customerRepository.deleteAll();
        customerRepository.flush();
    }

    @Test
    void shouldCreateCustomerThroughFullApplication() throws Exception {

        mockMvc.perform(
                post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "Integration",
                                    "lastName": "Test",
                                    "email": "integration@test.com",
                                    "phone": "09012345678"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.firstName").value("Integration"))
                .andExpect(jsonPath("$.lastName").value("Test"))
                .andExpect(jsonPath("$.email").value("integration@test.com"));

        Customer savedCustomer = customerRepository
                .findByEmail("integration@test.com")
                .orElseThrow();

        assertThat(savedCustomer.getFirstName())
                .isEqualTo("Integration");

        assertThat(savedCustomer.getLastName())
                .isEqualTo("Test");

        assertThat(savedCustomer.getEmail())
                .isEqualTo("integration@test.com");
    }

    @Test
    void shouldGetCustomerThroughFullApplication() throws Exception {

        Customer customer = new Customer();

        customer.setFirstName("Integration");
        customer.setLastName("Get");
        customer.setEmail("integration-get@test.com");
        customer.setPhone("09012345678");
        customer.setStatus(CustomerStatus.ACTIVE);

        Customer savedCustomer = customerRepository.saveAndFlush(customer);

        mockMvc.perform(
                get("/api/v1/customers/" + savedCustomer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(savedCustomer.getId()))
                .andExpect(jsonPath("$.firstName")
                        .value("Integration"))
                .andExpect(jsonPath("$.lastName")
                        .value("Get"))
                .andExpect(jsonPath("$.email")
                        .value("integration-get@test.com"))
                .andExpect(jsonPath("$.status")
                        .value("ACTIVE"));
    }

    @Test
    void shouldReturnNotFoundWhenCustomerDoesNotExist() throws Exception {

        mockMvc.perform(
                get("/api/v1/customers/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectDuplicateEmailThroughFullApplication() throws Exception {

        Customer existingCustomer = new Customer();

        existingCustomer.setFirstName("Existing");
        existingCustomer.setLastName("Customer");
        existingCustomer.setEmail("duplicate-integration@test.com");
        existingCustomer.setPhone("09000000001");
        existingCustomer.setStatus(CustomerStatus.ACTIVE);

        customerRepository.saveAndFlush(existingCustomer);

        mockMvc.perform(
                post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "Second",
                                    "lastName": "Customer",
                                    "email": "duplicate-integration@test.com",
                                    "phone": "09000000002"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldUpdateCustomerThroughFullApplication() throws Exception {

        Customer customer = new Customer();

        customer.setFirstName("Original");
        customer.setLastName("Customer");
        customer.setEmail("integration-update@test.com");
        customer.setPhone("09000000001");
        customer.setStatus(CustomerStatus.ACTIVE);

        Customer savedCustomer = customerRepository.saveAndFlush(customer);

        mockMvc.perform(
                patch("/api/v1/customers/" + savedCustomer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "Updated"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(savedCustomer.getId()))
                .andExpect(jsonPath("$.firstName")
                        .value("Updated"));

        Customer updatedCustomer = customerRepository
                .findById(savedCustomer.getId())
                .orElseThrow();

        assertThat(updatedCustomer.getFirstName())
                .isEqualTo("Updated");

        assertThat(updatedCustomer.getLastName())
                .isEqualTo("Customer");

        assertThat(updatedCustomer.getEmail())
                .isEqualTo("integration-update@test.com");
    }

    @Test
    void shouldDeleteCustomerThroughFullApplication() throws Exception {

        Customer customer = new Customer();

        customer.setFirstName("Delete");
        customer.setLastName("Integration");
        customer.setEmail("integration-delete@test.com");
        customer.setPhone("09000000001");
        customer.setStatus(CustomerStatus.ACTIVE);

        Customer savedCustomer = customerRepository.saveAndFlush(customer);

        Long customerId = savedCustomer.getId();

        mockMvc.perform(
                delete("/api/v1/customers/" + customerId))
                .andExpect(status().isNoContent());

        assertThat(customerRepository.findById(customerId))
                .isEmpty();
    }

    @Test
    void shouldGetCustomersWithPaginationThroughFullApplication() throws Exception {

        for (int i = 1; i <= 3; i++) {

            Customer customer = new Customer();

            customer.setFirstName("Customer" + i);
            customer.setLastName("Integration");
            customer.setEmail("pagination" + i + "@test.com");
            customer.setPhone("0900000000" + i);
            customer.setStatus(CustomerStatus.ACTIVE);

            customerRepository.save(customer);
        }

        customerRepository.flush();

        mockMvc.perform(
                get("/api/v1/customers")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void shouldRejectInvalidCustomerThroughFullApplication() throws Exception {

        mockMvc.perform(
                post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "",
                                    "lastName": "Integration",
                                    "email": "valid@test.com",
                                    "phone": "09012345678"
                                }
                                """))
                .andExpect(status().isBadRequest());

        assertThat(
                customerRepository.findByEmail("valid@test.com"))
                .isEmpty();
    }

    @Test
    void shouldReturnNotFoundForMissingCustomer() throws Exception {

        mockMvc.perform(
                get("/api/v1/customers/999999"))
                .andExpect(status().isNotFound());
    }
}