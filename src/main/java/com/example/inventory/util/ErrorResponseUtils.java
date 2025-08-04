package com.example.inventory.util;

import com.example.inventory.exception.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class ErrorResponseUtils {

    public static ResponseEntity<Object> buildError(HttpStatus status, String message, HttpServletRequest request) {
        ApiError apiError = new ApiError(status.getReasonPhrase(), request.getRequestURI());

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", status.value());
        responseBody.put("message", message);
        responseBody.put("error", apiError);

        return new ResponseEntity<>(responseBody, status);
    }

    public static ResponseEntity<Object> buildValidationError(HttpStatus status, String message, HttpServletRequest request, Map<String, String> validationErrors) {
        ApiError apiError = new ApiError(status.getReasonPhrase(), request.getRequestURI());

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", status.value());
        responseBody.put("message", message);
        responseBody.put("error", apiError);
        responseBody.put("validationErrors", validationErrors);

        return new ResponseEntity<>(responseBody, status);
    }
}
