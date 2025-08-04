package com.example.inventory.exception;

import java.time.LocalDateTime;

public class ApiError {
    private String error;
    private String path;
    private LocalDateTime timestamp;

    public ApiError(String error, String path) {
        this.timestamp = LocalDateTime.now();
        this.error = error;
        this.path = path;
    }

    public String getError() {
        return error;
    }

    public String getPath() {
        return path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
