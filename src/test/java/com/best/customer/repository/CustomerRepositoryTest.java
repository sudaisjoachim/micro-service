package com.best.customer.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.best.customer.entity.Customer;
import com.best.customer.entity.CustomerStatus;

@Testcontainers
@SpringBootTest
class CustomerRepositoryTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("customer_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private CustomerRepository customerRepository;

    // Test saving a customer
    @Test
    void shouldSaveCustomer() {

        Customer customer = new Customer();

        customer.setFirstName("Test");
        customer.setLastName("Customer");
        customer.setEmail("repository-test@example.com");
        customer.setPhone("09000000000");
        customer.setStatus(CustomerStatus.ACTIVE);

        Instant now = Instant.now();
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);

        Customer savedCustomer = customerRepository.saveAndFlush(customer);

        assertThat(savedCustomer.getId()).isNotNull();
        assertThat(savedCustomer.getEmail()).isEqualTo("repository-test@example.com");
    }

    // Test finding a customer by email
    @Test
    void shouldFindCustomerByEmail() {

        Customer customer = new Customer();

        customer.setFirstName("Jagwa");
        customer.setLastName("Denki");
        customer.setEmail("jagwa@denki.com");
        customer.setPhone("09012345678");
        customer.setStatus(com.best.customer.entity.CustomerStatus.ACTIVE);

        Customer savedCustomer = customerRepository.save(customer);

        var result = customerRepository.findByEmail("jagwa@denki.com");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedCustomer.getId());
        assertThat(result.get().getEmail()).isEqualTo("jagwa@denki.com");
    }

    // Test finding a customer by email when the email does not exist
    @Test
    void shouldReturnEmptyWhenCustomerEmailDoesNotExist() {

        var customer = customerRepository.findByEmail("does-not-exist@example.com");

        assertThat(customer).isEmpty();
    }

    // Test rejecting duplicate email
    @Test
    void shouldRejectDuplicateEmail() {

        Customer firstCustomer = new Customer();
        firstCustomer.setFirstName("First");
        firstCustomer.setLastName("Customer");
        firstCustomer.setEmail("duplicate@test.com");
        firstCustomer.setPhone("09000000001");
        firstCustomer.setStatus(CustomerStatus.ACTIVE);

        Instant now = Instant.now();
        firstCustomer.setCreatedAt(now);
        firstCustomer.setUpdatedAt(now);

        customerRepository.saveAndFlush(firstCustomer);

        Customer secondCustomer = new Customer();
        secondCustomer.setFirstName("Second");
        secondCustomer.setLastName("Customer");
        secondCustomer.setEmail("duplicate@test.com");
        secondCustomer.setPhone("09000000002");
        secondCustomer.setStatus(CustomerStatus.ACTIVE);
        secondCustomer.setCreatedAt(now);
        secondCustomer.setUpdatedAt(now);

        assertThatThrownBy(() -> customerRepository.saveAndFlush(secondCustomer))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // Test to check if email exists
    @Test
    void shouldReturnTrueWhenEmailExists() {

        Customer customer = new Customer();
        customer.setFirstName("Test");
        customer.setLastName("Customer");
        customer.setEmail("exists-test@example.com");
        customer.setPhone("09000000001");
        customer.setStatus(CustomerStatus.ACTIVE);

        customerRepository.saveAndFlush(customer);

        boolean exists = customerRepository.existsByEmail("exists-test@example.com");

        assertThat(exists).isTrue();
    }

    // Test to check if email does not exist
    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {

        boolean exists = customerRepository.existsByEmail("does-not-exist@example.com");

        assertThat(exists).isFalse();
    }

    // Test to check if email belongs to the same customer
    @Test
    void shouldReturnFalseWhenEmailBelongsToSameCustomer() {

        Customer customer = new Customer();
        customer.setFirstName("Test");
        customer.setLastName("Customer");
        customer.setEmail("same-customer@example.com");
        customer.setPhone("09000000001");
        customer.setStatus(CustomerStatus.ACTIVE);

        Customer savedCustomer = customerRepository.saveAndFlush(customer);

        boolean exists = customerRepository.existsByEmailAndIdNot(
                savedCustomer.getEmail(),
                savedCustomer.getId());

        assertThat(exists).isFalse();
    }

    // Test to check if email belongs to another customer
    @Test
    void shouldReturnTrueWhenEmailBelongsToAnotherCustomer() {

        Customer customer = new Customer();
        customer.setFirstName("First");
        customer.setLastName("Customer");
        customer.setEmail("duplicate-check@example.com");
        customer.setPhone("09000000001");
        customer.setStatus(CustomerStatus.ACTIVE);

        Customer savedCustomer = customerRepository.saveAndFlush(customer);

        boolean exists = customerRepository.existsByEmailAndIdNot(
                savedCustomer.getEmail(),
                999L);

        assertThat(exists).isTrue();
    }

}