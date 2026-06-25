package com.smartswine.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class OrderRequest {
    private String deliveryAddress;
    private LocalDate deliveryDate;
    private String notes;
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
    }
}
