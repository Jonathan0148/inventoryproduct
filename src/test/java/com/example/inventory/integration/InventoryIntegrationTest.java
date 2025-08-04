package com.example.inventory.integration;

import com.example.inventory.client.ProductClient;
import com.example.inventory.dto.ProductDTO;
import com.example.inventory.model.Inventory;
import com.example.inventory.repository.InventoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "api.keys.frontend=pk_g0b5e7c9d7a8411b8a2c3b92ha6t85j8"
})
public class InventoryIntegrationTest {

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String INVENTORY_API_KEY = "pk_g0b5e7c9d7a8411b8a2c3b92ha6t85j8";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductClient productClient;

    @Autowired
    private InventoryRepository inventoryRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ProductDTO product;

    @BeforeEach
    void setUp() {
        // Clean and prepare
        inventoryRepository.deleteAll();

        product = new ProductDTO();
        product.setId(1L);
        product.setName("Producto Mock");
        product.setDescription("Descripción Mock");
        product.setPrice(BigDecimal.valueOf(50.0));

        // Mock for existing and non-existing products
        when(productClient.getProductById(1L)).thenReturn(product);
        when(productClient.getProductById(999L))
                .thenThrow(new com.example.inventory.exception.ResourceNotFoundException(
                        "Producto no encontrado en el MS1 con ID: 999"));
    }

    @Test
    void GET_inventory_returnsZeroWhenNotExists() throws Exception {
        mockMvc.perform(get("/api/inventory/1")
                        .header(API_KEY_HEADER, INVENTORY_API_KEY)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(0))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void PUT_updateQuantity_createsOrUpdatesInventory() throws Exception {
        String json = "{\"quantity\":20}";

        mockMvc.perform(put("/api/inventory/1")
                        .header(API_KEY_HEADER, INVENTORY_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(20));

        Inventory saved = inventoryRepository.findByProductId(1L).orElseThrow();
        assert saved.getQuantity() == 20;
    }

    @Test
    void POST_purchase_success_andUpdatesStock() throws Exception {
        inventoryRepository.save(new Inventory(1L, 15));

        String body = "{\"productId\":1,\"quantity\":5}";

        mockMvc.perform(post("/api/inventory/purchase")
                        .header(API_KEY_HEADER, INVENTORY_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.remainingStock").value(10));

        Inventory updated = inventoryRepository.findByProductId(1L).orElseThrow();
        assert updated.getQuantity() == 10;
    }

    @Test
    void POST_purchase_insufficientStock_returnsBadRequest() throws Exception {
        inventoryRepository.save(new Inventory(1L, 2));

        String body = "{\"productId\":1,\"quantity\":5}";

        mockMvc.perform(post("/api/inventory/purchase")
                        .header(API_KEY_HEADER, INVENTORY_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Inventario insuficiente para el producto ID: 1"));
    }

    @Test
    void GET_inventory_productNotFound_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/inventory/999")
                        .header(API_KEY_HEADER, INVENTORY_API_KEY)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void PUT_updateQuantity_productNotFound_returnsNotFound() throws Exception {
        String json = "{\"quantity\":5}";

        mockMvc.perform(put("/api/inventory/999")
                        .header(API_KEY_HEADER, INVENTORY_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }
}
