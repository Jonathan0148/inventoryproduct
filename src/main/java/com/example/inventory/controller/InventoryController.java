package com.example.inventory.controller;

import com.example.inventory.dto.ApiResponse;
import com.example.inventory.dto.ProductWithInventoryDTO;
import com.example.inventory.dto.PurchaseRequest;
import com.example.inventory.dto.PurchaseResponse;
import com.example.inventory.model.Inventory;
import com.example.inventory.service.InventoryService;
import com.example.inventory.util.SuccessResponseUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventario", description = "Operaciones relacionadas con el inventario de productos")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Operation(
            summary = "Obtener inventario por ID de producto",
            description = "Consulta el inventario para un producto específico. Si el producto existe pero no tiene inventario, retorna cantidad 0.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Inventario obtenido correctamente",
                            content = @Content(schema = @Schema(implementation = ProductWithInventoryDTO.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Producto no encontrado")
            }
    )
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductWithInventoryDTO>> getInventoryByProductId(
            @Parameter(description = "ID del producto a consultar", example = "1")
            @PathVariable Long productId
    ) {
        ProductWithInventoryDTO data = inventoryService.getInventoryWithProductCheck(productId);
        return SuccessResponseUtils.buildOk(data, "Inventario obtenido correctamente");
    }

    @Operation(
            summary = "Actualizar cantidad de inventario de un producto",
            description = "Permite actualizar directamente la cantidad en inventario para un producto existente.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Objeto con la nueva cantidad de inventario",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Inventory.class))
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cantidad actualizada correctamente",
                            content = @Content(schema = @Schema(implementation = Inventory.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Error de validación"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Producto o inventario no encontrado")
            }
    )
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<Inventory>> updateQuantity(
            @Parameter(description = "ID del producto a actualizar", example = "1")
            @PathVariable Long productId,
            @Valid @RequestBody Inventory inventory
    ) {
        Inventory updated = inventoryService.updateQuantity(productId, inventory);
        return SuccessResponseUtils.buildOk(updated, "Cantidad de inventario actualizada correctamente");
    }

    @Operation(
            summary = "Realizar una compra de producto",
            description = "Verifica la disponibilidad en inventario, descuenta la cantidad comprada y devuelve información de la compra.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Solicitud de compra con ID de producto y cantidad",
                    required = true,
                    content = @Content(schema = @Schema(implementation = PurchaseRequest.class))
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Compra procesada correctamente",
                            content = @Content(schema = @Schema(implementation = PurchaseResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Error de validación o inventario insuficiente"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Producto o inventario no encontrado")
            }
    )
    @PostMapping("/purchase")
    public ResponseEntity<ApiResponse<PurchaseResponse>> processPurchase(
            @Valid @RequestBody PurchaseRequest request
    ) {
        PurchaseResponse response = inventoryService.processPurchase(request);
        return SuccessResponseUtils.buildOk(response, "Compra procesada correctamente");
    }
}
