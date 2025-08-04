package com.example.inventory.controller;

import com.example.inventory.dto.ApiResponse;
import com.example.inventory.dto.ProductWithInventoryDTO;
import com.example.inventory.dto.PurchaseRequest;
import com.example.inventory.dto.PurchaseResponse;
import com.example.inventory.model.Inventory;
import com.example.inventory.service.InventoryService;
import com.example.inventory.util.SuccessResponseUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductWithInventoryDTO>> getInventoryByProductId(@PathVariable Long productId) {
        ProductWithInventoryDTO data = inventoryService.getInventoryWithProductCheck(productId);
        return SuccessResponseUtils.buildOk(data, "Inventario obtenido correctamente");
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<Inventory>> updateQuantity(
            @PathVariable Long productId,
            @Valid @RequestBody Inventory inventory
    ) {
        Inventory updated = inventoryService.updateQuantity(productId, inventory);
        return SuccessResponseUtils.buildOk(updated, "Cantidad de inventario actualizada correctamente");
    }

    @PostMapping("/purchase")
    public ResponseEntity<ApiResponse<PurchaseResponse>> processPurchase(
            @Valid @RequestBody PurchaseRequest request
    ) {
        PurchaseResponse response = inventoryService.processPurchase(request);
        return SuccessResponseUtils.buildOk(response, "Compra procesada correctamente");
    }
}
