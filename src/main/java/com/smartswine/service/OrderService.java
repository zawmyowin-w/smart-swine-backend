package com.smartswine.service;

import com.smartswine.dto.request.OrderRequest;
import com.smartswine.model.*;
import com.smartswine.model.enums.OrderStatus;
import com.smartswine.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public List<Order> findAll() { return orderRepository.findAll(); }

    public Order findById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    public List<Order> findByCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public List<Order> findByStatus(OrderStatus status) { return orderRepository.findByStatus(status); }

    @Transactional
    public Order createFromCart(OrderRequest req, Long customerId) {
        User customer = userRepository.findById(customerId).orElseThrow(() -> new RuntimeException("Customer not found"));

        String orderNumber = generateOrderNumber();
        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderRequest.OrderItemRequest itemReq : req.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemReq.getProductId()));
            if (product.getStockQuantity() < itemReq.getQuantity()) {
                throw new RuntimeException("Insufficient stock for: " + product.getName());
            }
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            total = total.add(subtotal);

            items.add(OrderItem.builder()
                .product(product)
                .quantity(itemReq.getQuantity())
                .unitPrice(product.getPrice())
                .subtotal(subtotal)
                .build());
        }

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .customer(customer)
                .totalAmount(total)
                .status(OrderStatus.PENDING)
                .deliveryAddress(req.getDeliveryAddress())
                .deliveryDate(req.getDeliveryDate())
                .notes(req.getNotes())
                .items(new ArrayList<>())
                .build();

        Order savedOrder = orderRepository.save(order);
        for (OrderItem item : items) {
            item.setOrder(savedOrder);
            savedOrder.getItems().add(item);
        }

        // Reserve stock
        for (OrderRequest.OrderItemRequest itemReq : req.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId()).get();
            product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());
            productRepository.save(product);
        }

        savedOrder.setStatus(OrderStatus.STOCK_RESERVED);
        Order finalOrder = orderRepository.save(savedOrder);
        auditService.log(customerId, customer.getUsername(), "CREATE_ORDER", "Order", finalOrder.getId(), null, orderNumber, null, null);
        return finalOrder;
    }

    @Transactional
    public Order updateStatus(Long id, OrderStatus status, Long userId) {
        Order order = findById(id);
        String oldStatus = order.getStatus().name();
        order.setStatus(status);
        Order saved = orderRepository.save(order);
        auditService.log(userId, null, "UPDATE_ORDER_STATUS", "Order", id, oldStatus, status.name(), null, null);
        return saved;
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "ORD-" + timestamp + "-" + random;
    }

    public long countByStatus(OrderStatus status) { return orderRepository.countByStatus(status); }
}
