package com.smartswine.controller;

import com.smartswine.dto.response.ApiResponse;
import com.smartswine.model.Product;
import com.smartswine.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> findAvailable() {
        return ResponseEntity.ok(ApiResponse.success(productRepository.findByIsAvailableTrueAndStockQuantityGreaterThan(0)));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<Product>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(productRepository.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"))));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Product>> create(@RequestBody Product product) {
        return ResponseEntity.ok(ApiResponse.success("Product created", productRepository.save(product)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Product>> update(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        return ResponseEntity.ok(ApiResponse.success("Product updated", productRepository.save(product)));
    }
}
