package com.smartswine.controller;

import com.smartswine.dto.response.ApiResponse;
import com.smartswine.model.InventoryItem;
import com.smartswine.repository.UserRepository;
import com.smartswine.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryItem>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.findAll()));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<InventoryItem>>> getLowStock() {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getLowStockItems()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<InventoryItem>> create(@RequestBody InventoryItem item) {
        return ResponseEntity.ok(ApiResponse.success("Inventory item created", inventoryService.create(item)));
    }

    @PostMapping("/{id}/add-stock")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<InventoryItem>> addStock(
            @PathVariable Long id,
            @RequestParam BigDecimal quantity,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Stock added", inventoryService.addStock(id, quantity, userId)));
    }

    @PostMapping("/{id}/use-stock")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'SUPERVISOR', 'STAFF')")
    public ResponseEntity<ApiResponse<InventoryItem>> useStock(
            @PathVariable Long id,
            @RequestParam BigDecimal quantity,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Stock used", inventoryService.useStock(id, quantity, userId)));
    }

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername()).map(u -> u.getId()).orElse(null);
    }
}
