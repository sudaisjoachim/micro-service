package com.best.customer.dto;

import java.util.HashSet;
import java.util.Set;

import com.best.customer.validation.ValidCustomerPatch;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;

@ValidCustomerPatch
@Getter
@NoArgsConstructor
public class CustomerPatchRequest {

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Email
    private String email;

    @Size(max = 30)
    private String phone;

    private final Set<String> fieldsPresent = new HashSet<>();

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        fieldsPresent.add("firstName");
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        fieldsPresent.add("lastName");
    }

    public void setEmail(String email) {
        this.email = email;
        fieldsPresent.add("email");
    }

    public void setPhone(String phone) {
        this.phone = phone;
        fieldsPresent.add("phone");
    }

    public boolean isPresent(String field) {
        return fieldsPresent.contains(field);
    }
}