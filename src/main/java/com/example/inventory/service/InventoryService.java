package com.example.inventory.service;

import com.example.inventory.client.ProductClient;
import com.example.inventory.dto.*;
import com.example.inventory.exception.InsufficientStockException;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.model.Inventory;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.util.SuccessResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductClient productClient;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository, ProductClient productClient) {
        this.inventoryRepository = inventoryRepository;
        this.productClient = productClient;
    }

    public ProductWithInventoryDTO getInventoryWithProductCheck(Long productId) {
        ProductDTO product;
        try {
            product = productClient.getProductById(productId);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Producto no encontrado en el MS1 con ID: " + productId);
        } catch (RestClientException e) {
            throw new RuntimeException("Error al consultar el producto en el MS1: " + e.getMessage());
        }

        Inventory inventory = inventoryRepository.findByProductId(productId).orElse(null);
        int quantity = (inventory != null) ? inventory.getQuantity() : 0;

        ProductWithInventoryDTO response = new ProductWithInventoryDTO();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setQuantity(quantity);

        return response;
    }

    public Inventory updateQuantity(Long productId, Inventory requestInventory) {
        ProductDTO product;
        try {
            product = productClient.getProductById(productId);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Producto no encontrado en el MS1 con ID: " + productId);
        } catch (RestClientException e) {
            throw new RuntimeException("Error al consultar el producto en el MS1: " + e.getMessage());
        }

        System.out.println("🟢 Producto recibido: " + product);

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElse(null);

        if (inventory == null) {
            inventory = new Inventory();
            inventory.setProductId(productId);
            inventory.setQuantity(requestInventory.getQuantity());
        } else {
            inventory.setQuantity(requestInventory.getQuantity());
        }

        return inventoryRepository.save(inventory);
    }

    public PurchaseResponse processPurchase(PurchaseRequest request) {
        Long productId = request.getProductId();
        int requestedQuantity = request.getQuantity();

        // 1. Verificar existencia del producto en MS1
        ProductDTO product;
        try {
            product = productClient.getProductById(productId);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Producto no encontrado en el MS1 con ID: " + productId);
        } catch (RestClientException e) {
            throw new RuntimeException("Error al consultar el producto en el MS1: " + e.getMessage());
        }

        // 2. Consultar inventario
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario no encontrado para el producto ID: " + productId));

        // 3. Verificar disponibilidad
        if (inventory.getQuantity() < requestedQuantity) {
            throw new InsufficientStockException("Inventario insuficiente para el producto ID: " + productId);
        }

        // 4. Actualizar inventario
        int remaining = inventory.getQuantity() - requestedQuantity;
        inventory.setQuantity(remaining);
        inventoryRepository.save(inventory);

        // 5. Retornar información de la compra
        PurchaseResponse response = new PurchaseResponse();
        response.setProductId(product.getId());
        response.setProductName(product.getName());
        response.setProductPrice(product.getPrice());
        response.setQuantityPurchased(requestedQuantity);
        response.setRemainingStock(remaining);

        return response;
    }
}
