package com.example.inventory.service;

import com.example.inventory.client.ProductClient;
import com.example.inventory.dto.ProductDTO;
import com.example.inventory.dto.PurchaseRequest;
import com.example.inventory.dto.PurchaseResponse;
import com.example.inventory.exception.InsufficientStockException;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.model.Inventory;
import com.example.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryServiceTest {

    @InjectMocks
    private InventoryService inventoryService;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductClient productClient;

    private Inventory existingInventory;
    private ProductDTO product;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        existingInventory = new Inventory();
        existingInventory.setId(1L);
        existingInventory.setProductId(1L);
        existingInventory.setQuantity(10);

        product = new ProductDTO();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Description");
        product.setPrice(BigDecimal.valueOf(99.99));
    }

    @Test
    void testUpdateQuantity_Success() {
        when(productClient.getProductById(1L)).thenReturn(product);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(existingInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));

        Inventory updated = new Inventory();
        updated.setQuantity(5);

        Inventory result = inventoryService.updateQuantity(1L, updated);

        assertThat(result.getQuantity()).isEqualTo(5);
        verify(inventoryRepository).save(existingInventory);
    }

    @Test
    void testUpdateQuantity_ProductNotFound() {
        Inventory requestInventory = new Inventory();
        requestInventory.setQuantity(10);

        when(productClient.getProductById(999L))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        assertThatThrownBy(() -> inventoryService.updateQuantity(999L, requestInventory))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Producto no encontrado en el MS1");
    }

    @Test
    void testGetInventoryWithProductCheck_WhenInventoryExists() {
        when(productClient.getProductById(1L)).thenReturn(product);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(existingInventory));

        var result = inventoryService.getInventoryWithProductCheck(1L);

        assertThat(result.getQuantity()).isEqualTo(10);
        assertThat(result.getName()).isEqualTo("Test Product");
    }

    @Test
    void testGetInventoryWithProductCheck_WhenInventoryDoesNotExist() {
        when(productClient.getProductById(1L)).thenReturn(product);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.empty());

        var result = inventoryService.getInventoryWithProductCheck(1L);

        assertThat(result.getQuantity()).isEqualTo(0);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void testGetInventoryWithProductCheck_ProductNotFound() {
        when(productClient.getProductById(1L)).thenThrow(new ResourceNotFoundException("Producto no encontrado"));

        assertThatThrownBy(() -> inventoryService.getInventoryWithProductCheck(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testProcessPurchase_Success() {
        PurchaseRequest request = new PurchaseRequest();
        request.setProductId(1L);
        request.setQuantity(3);

        when(productClient.getProductById(1L)).thenReturn(product);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(existingInventory));
        when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PurchaseResponse response = inventoryService.processPurchase(request);

        assertThat(response.getProductId()).isEqualTo(1L);
        assertThat(response.getQuantityPurchased()).isEqualTo(3);
        assertThat(response.getRemainingStock()).isEqualTo(7);
    }

    @Test
    void testProcessPurchase_InventoryInsufficient() {
        PurchaseRequest request = new PurchaseRequest();
        request.setProductId(1L);
        request.setQuantity(50);

        when(productClient.getProductById(1L)).thenReturn(product);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(existingInventory));

        assertThatThrownBy(() -> inventoryService.processPurchase(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Inventario insuficiente para el producto ID");
    }

    @Test
    void testProcessPurchase_ProductNotFound() {
        PurchaseRequest request = new PurchaseRequest();
        request.setProductId(1L);
        request.setQuantity(3);

        when(productClient.getProductById(1L)).thenThrow(new ResourceNotFoundException("Producto no encontrado"));

        assertThatThrownBy(() -> inventoryService.processPurchase(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
