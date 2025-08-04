package com.example.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta estándar de la API con mensaje, datos y código de estado")
public class ApiResponse<T> {

    @Schema(description = "Mensaje de estado o información adicional", example = "Operación realizada con éxito")
    private String message;

    @Schema(description = "Datos devueltos por la operación")
    private T data;

    @Schema(description = "Código de estado HTTP de la respuesta", example = "200")
    private int status;

    public ApiResponse() {
    }

    public ApiResponse(String message, T data, int status) {
        this.message = message;
        this.data = data;
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public int getStatus() {
        return status;
    }
}
