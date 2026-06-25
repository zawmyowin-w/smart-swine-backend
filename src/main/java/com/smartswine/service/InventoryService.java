package com.smartswine.service;

import com.smartswine.model.InventoryItem;
import com.smartswine.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final AuditService auditService;

    public List<InventoryItem> findAll() { return inventoryItemRepository.findByIsActiveTrue(); }

    public List<InventoryItem> findByCategory(InventoryItem.Category category) {
        return inventoryItemRepository.findByCategory(category);
    }

    public List<InventoryItem> getLowStockItems() {
        return inventoryItemRepository.findLowStockItems();
    }

    public InventoryItem findById(Long id) {
        return inventoryItemRepository.findById(id).orElseThrow(() -> new RuntimeException("Inventory item not found"));
    }

    @Transactional
    public InventoryItem create(InventoryItem item) {
        return inventoryItemRepository.save(item);
    }

    @Transactional
    public InventoryItem addStock(Long id, BigDecimal quantity, Long userId) {
        InventoryItem item = findById(id);
        item.setCurrentStock(item.getCurrentStock().add(quantity));
        auditService.log(userId, null, "INVENTORY_IN", "InventoryItem", id, null, "+" + quantity, null, null);
        return inventoryItemRepository.save(item);
    }

    @Transactional
    public InventoryItem useStock(Long id, BigDecimal quantity, Long userId) {
        InventoryItem item = findById(id);
        if (item.getCurrentStock().compareTo(quantity) < 0) {
            throw new RuntimeException("Insufficient stock for: " + item.getName());
        }
        item.setCurrentStock(item.getCurrentStock().subtract(quantity));
        auditService.log(userId, null, "INVENTORY_OUT", "InventoryItem", id, null, "-" + quantity, null, null);
        return inventoryItemRepository.save(item);
    }
}
