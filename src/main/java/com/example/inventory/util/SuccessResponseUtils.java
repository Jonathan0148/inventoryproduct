package com.example.inventory.util;

import com.example.inventory.dto.ApiResponse;
import com.example.inventory.dto.PaginatedResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class SuccessResponseUtils {

    public static <T> ResponseEntity<ApiResponse<T>> buildOk(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>(message, data, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    public static <T> ResponseEntity<ApiResponse<T>> buildCreated(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>(message, data, HttpStatus.CREATED.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public static <T> ResponseEntity<ApiResponse<Void>> buildNoContent(String message) {
        ApiResponse<Void> response = new ApiResponse<>(message, null, HttpStatus.NO_CONTENT.value());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    public static <T> ResponseEntity<ApiResponse<PaginatedResponse<T>>> buildPaginated(Page<T> page, String message) {
        PaginatedResponse<T> paginated = new PaginatedResponse<>(
                page.getContent(),
                new PaginatedResponse.Meta(
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalPages(),
                        page.getTotalElements()
                )
        );

        ApiResponse<PaginatedResponse<T>> response = new ApiResponse<>(message, paginated, HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }
}
