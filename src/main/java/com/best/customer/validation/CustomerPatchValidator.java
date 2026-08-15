package com.best.customer.validation;

import com.best.customer.dto.CustomerPatchRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CustomerPatchValidator implements ConstraintValidator<ValidCustomerPatch, CustomerPatchRequest> {

    @Override
    public boolean isValid(CustomerPatchRequest request, ConstraintValidatorContext context) {

        if (request == null) {
            return true;
        }

        context.disableDefaultConstraintViolation();

        boolean valid = true;

        if (request.isPresent("firstName")) {
            if (request.getFirstName() == null || request.getFirstName().isBlank()) {

                addError(context, "firstName", "must not be blank");
                valid = false;
            }
        }

        if (request.isPresent("lastName")) {
            if (request.getLastName() == null
                    || request.getLastName().isBlank()) {

                addError(context, "lastName", "must not be blank");
                valid = false;
            }
        }

        if (request.isPresent("email")) {
            if (request.getEmail() == null
                    || request.getEmail().isBlank()) {

                addError(context, "email", "must not be blank");
                valid = false;
            }
        }

        if (request.isPresent("phone")) {
            if (request.getPhone() != null
                    && request.getPhone().isBlank()) {

                addError(context, "phone", "must not be blank");
                valid = false;
            }
        }

        return valid;
    }

    private void addError(ConstraintValidatorContext context,
            String field,
            String message) {

        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
    }
}