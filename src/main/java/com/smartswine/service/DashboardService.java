package com.smartswine.service;

import com.smartswine.model.enums.OrderStatus;
import com.smartswine.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PigRepository pigRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final BirthRepository birthRepository;
    private final PregnancyRepository pregnancyRepository;

    public Map<String, Object> getSuperAdminDashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", userRepository.count());
        data.put("totalPigs", pigRepository.count());
        data.put("totalOrders", orderRepository.count());
        data.put("pendingOrders", orderRepository.countByStatus(OrderStatus.PENDING));
        data.put("completedOrders", orderRepository.countByStatus(OrderStatus.COMPLETED));
        data.put("lowStockItems", inventoryItemRepository.findLowStockItems().size());

        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        BigDecimal monthlyRevenue = orderRepository.sumRevenueByDateRange(startOfMonth, LocalDateTime.now());
        data.put("monthlyRevenue", monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO);
        return data;
    }

    public Map<String, Object> getPigDashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("total", pigRepository.count());
        data.put("pendingBirths", birthRepository.findByStatus(com.smartswine.model.Birth.Status.PENDING_HR).size());
        data.put("activePregnancies", pregnancyRepository.findByStatus(com.smartswine.model.Pregnancy.Status.CONFIRMED).size());
        return data;
    }

    public Map<String, Object> getFinanceDashboard() {
        Map<String, Object> data = new HashMap<>();
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        BigDecimal monthlyRevenue = orderRepository.sumRevenueByDateRange(startOfMonth, LocalDateTime.now());
        data.put("monthlyRevenue", monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO);
        data.put("pendingPayments", 0);
        return data;
    }
}
