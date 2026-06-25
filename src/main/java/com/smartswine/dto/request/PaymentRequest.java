package com.smartswine.dto.request;

import com.smartswine.model.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotNull
    private Long orderId;
    @NotNull
    private PaymentMethod method;
    private String transactionRef;
    private String notes;
}
