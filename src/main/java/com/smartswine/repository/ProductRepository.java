package com.smartswine.repository;

import com.smartswine.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByIsAvailableTrue();
    List<Product> findByCategory(Product.Category category);
    List<Product> findByIsAvailableTrueAndStockQuantityGreaterThan(int quantity);
    List<Product> findByNameContainingIgnoreCase(String name);
}
