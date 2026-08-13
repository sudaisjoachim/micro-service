package com.best.customer.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(CustomerEmailAlreadyExistsException.class)
        public ResponseEntity<ApiErrorResponse> handleCustomerEmailAlreadyExists(
                        CustomerEmailAlreadyExistsException ex,
                        WebRequest request) {

                ApiErrorResponse error = new ApiErrorResponse(
                                HttpStatus.CONFLICT.value(),
                                "CUSTOMER_EMAIL_ALREADY_EXISTS",
                                ex.getMessage(),
                                request.getDescription(false).replace("uri=", ""));

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(error);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiErrorResponse> handleValidation(
                        MethodArgumentNotValidException ex,
                        WebRequest request) {

                Map<String, String> errors = new HashMap<>();

                ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(
                                error.getField(),
                                error.getDefaultMessage()));

                ApiErrorResponse response = new ApiErrorResponse(
                                Instant.now(),
                                HttpStatus.BAD_REQUEST.value(),
                                "VALIDATION_ERROR",
                                "Request validation failed",
                                request.getDescription(false).replace("uri=", ""),
                                errors);

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(response);
        }

        @ExceptionHandler(CustomerNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleCustomerNotFound(
                        CustomerNotFoundException ex,
                        WebRequest request) {

                ApiErrorResponse response = new ApiErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "CUSTOMER_NOT_FOUND",
                                ex.getMessage(),
                                request.getDescription(false).replace("uri=", ""));

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(response);
        }
}
