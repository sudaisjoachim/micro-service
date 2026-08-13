package com.best.customer.exception;

public class CustomerEmailAlreadyExistsException extends RuntimeException {

    public CustomerEmailAlreadyExistsException(String email) {
        super("A customer with email '" + email + "' already exists");
    }
}
