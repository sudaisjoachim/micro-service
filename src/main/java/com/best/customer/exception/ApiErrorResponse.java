package com.best.customer.exception;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        Map<String, String> errors) {

    public ApiErrorResponse(
            int status,
            String code,
            String message,
            String path) {
        this(Instant.now(), status, code, message, path, null);
    }
}
