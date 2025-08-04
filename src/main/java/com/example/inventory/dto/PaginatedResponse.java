package com.example.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Respuesta paginada con elementos y metadatos de paginación")
public class PaginatedResponse<T> {

    @Schema(description = "Lista de elementos de la página actual")
    private List<T> items;

    @Schema(description = "Información sobre la paginación")
    private Meta meta;

    public PaginatedResponse(List<T> items, Meta meta) {
        this.items = items;
        this.meta = meta;
    }

    public List<T> getItems() {
        return items;
    }

    public Meta getMeta() {
        return meta;
    }

    @Schema(description = "Metadatos sobre la paginación de la respuesta")
    public static class Meta {

        @Schema(description = "Número de la página actual (0 indexado)", example = "0")
        private int page;

        @Schema(description = "Tamaño de la página (cantidad de elementos por página)", example = "10")
        private int size;

        @Schema(description = "Número total de páginas disponibles", example = "5")
        private int totalPages;

        @Schema(description = "Cantidad total de elementos", example = "50")
        private long totalElements;

        public Meta(int page, int size, int totalPages, long totalElements) {
            this.page = page;
            this.size = size;
            this.totalPages = totalPages;
            this.totalElements = totalElements;
        }

        public int getPage() {
            return page;
        }

        public int getSize() {
            return size;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public long getTotalElements() {
            return totalElements;
        }
    }
}
