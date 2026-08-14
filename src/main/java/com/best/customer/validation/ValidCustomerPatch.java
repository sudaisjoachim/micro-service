package com.best.customer.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CustomerPatchValidator.class)
@Documented
public @interface ValidCustomerPatch {

    String message() default "Invalid customer update";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
